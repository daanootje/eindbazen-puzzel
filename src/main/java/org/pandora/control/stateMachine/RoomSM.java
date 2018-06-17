package org.pandora.control.stateMachine;

import gnu.io.SerialPortEvent;
import lombok.Builder;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.pandora.control.clock.CountDown;
import org.pandora.control.data.DataManager;
import org.pandora.control.data.DataObject;
import org.pandora.control.data.PuzzleData;
import org.pandora.control.hints.HintManager;
import org.pandora.control.music.AudioManager;
import org.pandora.control.puzzle.Puzzle;
import org.pandora.control.puzzle.PuzzleManager;
import org.pandora.control.serialcomm.SerialCommunicator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Slf4j
@Component
@EnableScheduling
public class RoomSM extends SerialCommunicator {

	@Autowired
	private StateMachine<String,String> stateMachine;
	private List<String> nonPuzzleStates = Arrays.asList("Initial", "InitCheck", "Idle", "Finalize", "Final");

	private ExecutorService EXECUTOR = Executors.newFixedThreadPool(10);
	private PuzzleManager puzzleManager;
	private CountDown timeRemaining;
	private HintManager hintManager;
	private DataManager dataManager;
	private AudioManager audioManager;

	private Boolean finished = false;
	private Boolean started = false;
	private Boolean reset = true;

	private StringBuilder bufferIncoming = new StringBuilder();
	private Queue<Package> queue = new LinkedList<>();

	private Map<String, PuzzleData> puzzlesData = new HashMap<>();
	private Boolean succeeded = false;

	@Autowired
	public RoomSM(CountDown timeRemaining, HintManager hintManager, AudioManager audioManager, PuzzleManager puzzleManager, DataManager dataManager) {
		super();
		this.timeRemaining = timeRemaining;
		this.puzzleManager = puzzleManager;
		this.hintManager = hintManager;
		this.dataManager = dataManager;
		this.audioManager = audioManager;
	}

	public Boolean getFinished() {
		return finished;
	}

	public void resetSM() {
		try {
			stopSM();
			checkForSerialPorts();
			EXECUTOR = Executors.newFixedThreadPool(10);
			bufferIncoming = new StringBuilder();
			queue = new LinkedList<>();
			stateMachine.stop();
			stateMachine.start();
			reset = true;
		} catch (Exception e) {
			log.error(String.format("Something went wrong initializing statemachine - %s", e.getMessage()));
		}
	}

	public void startPuzzles() {
		if(started) {
			stateMachine.sendEvent("Idle_Signal");
			log.info("Starting timer");
			timeRemaining.start();
			log.info("Starting audio");
			audioManager.playMusic("backgroundMusic");
		}
	}

	public void startSM(String port) {
		if(reset) {
			stateMachine.addStateListener(new StateMachineEventListener());
			initializePort(port);
			stateMachine.start();
			finished = false;
			started = true;
			reset = false;
		}
	}

	public void stopSM() {
		stateMachine.stop();
	}

	public void finishSM() {
		finishPuzzles();
		stateMachine.stop();
		succeeded = false;
		endSM();
	}

	public String getState() {
		if (!reset) {
			return stateMachine.getState().getId();
		} else {
			return "Statemachine not started";
		}
	}

	public void setPuzzleState(String puzzleName, String state) {
		sendCommandToPuzzle(puzzleName, state);
		Optional<String> name = puzzleManager.getPuzzleName(puzzleName);
		if (state.equals("finish") && name.isPresent()) {
			puzzlesData.put(name.get(), PuzzleData.builder().succeeded(false).build());
		}
	}

	private void finishPuzzle(String puzzleName) {
		PuzzleData.PuzzleDataBuilder puzzleData = PuzzleData.builder()
				.name(puzzleName)
				.hints(hintManager.getNumberOfHints(puzzleName));
		if (!puzzlesData.containsKey(puzzleName)) {
			puzzleData.succeeded(true);
		}
		puzzlesData.put(puzzleName, puzzleData.build());
	}

	@Override
	public void serialEvent(SerialPortEvent evt) {
		if (evt.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
			try {
				byte[] bytes = new byte[20];
				getInput().read(bytes);
				String s = new String(bytes, StandardCharsets.UTF_8);
				bufferIncoming.append(s.replaceAll("\u0000.*", ""));
			} catch (IOException e) {
				log.error("Failed to read incoming data - %s", e.getMessage());
			}
		}
	}

	@Scheduled(fixedRate = 100)
	private void checkBuffer() {
		if (bufferIncoming.length() == 0) {
			return;
		}

		StringBuilder recvData = new StringBuilder();
		int length = 0;
		Boolean process = false;
		for (int i = 0; i < bufferIncoming.length(); i++) {
			recvData.append(bufferIncoming.charAt(i));
			if (bufferIncoming.charAt(i) == '!') {

				queue.add(Package.builder()
						.identifier(String.valueOf(recvData.charAt(0)))
						.message(recvData.substring(1, recvData.length()-1))
						.build()
				);
				length += recvData.length();
				recvData = new StringBuilder();
				process = true;
			}
		}
		if(process) {
			bufferIncoming.delete(0, length);
			processQueue();
		}
	}

	private void processQueue() {
		Package aPackage;
		while ((aPackage = queue.poll()) != null) {
			log.info(String.format("Processing package for %s with message: '%s'",aPackage.getIdentifier(), aPackage.getMessage()));
			applyPuzzle_PC(aPackage.getIdentifier(), aPackage.getMessage());
		}
	}

	private Future applyPuzzle_PC(String identifier, String message) {
		return EXECUTOR.submit(() -> puzzleManager.applyToPuzzle(identifier, message)
				.ifPresent(pair -> {
					if(pair.getValue1().equals("finished")) {
						stateMachine.sendEvent(String.format("%s_Signal", pair.getValue0()));
						finishPuzzle(pair.getValue0());
					}
				}));
	}

	private Future initPuzzles() {
		return EXECUTOR.submit(() -> sendCommandToAllPuzzles("reset"));
	}

	private Future finishPuzzles() {
		return EXECUTOR.submit(() -> sendCommandToAllPuzzles("finish"));
	}

	private Future sendCommandToPuzzle(String puzzleName, String name) {
		return EXECUTOR.submit(() -> puzzleManager.getPC_PuzzleCommand(puzzleName, name)
				.ifPresent(pair -> writeData(pair.getValue0(), pair.getValue1()))
		);
	}

	private void sendCommandToAllPuzzles(String name) {
		puzzleManager.getPuzzles().values()
				.forEach(puzzle -> puzzle.getPC_PuzzleCommand(name)
                        .ifPresent(s -> writeData(puzzle.getIdentifier(), s))
				);
	}

	private Future checkInitPuzzles() {
		return EXECUTOR.submit(() -> {
			Boolean init = false;
			Boolean resent = false;
			Integer time = 0;
			while(!init) {
				try {
					if (!resent && time >= 30000) {
						log.info("30s passed without passing initialization of all puzzles, resetting puzzles which did not pass initial check");
						stateMachine.getStates().stream()
								.filter(state -> !state.getId().toLowerCase().contains("final"))
								.filter(state -> puzzleManager.getPuzzleState(state.getId())
										.map(puzzleState -> {
											log.info(String.format("%s did not pass init check", state));
											return puzzleState.equals("stopped");
										})
										.orElse(false)
								)
								.forEach(state -> sendCommandToPuzzle(state.getId(), "reset"));
						resent = true;
					} else if (resent && time >= 60000) {
						log.info("60s passed without passing initialization of all puzzles, stopping statemachine");
						stopSM();
						break;
					}
					init = stateMachine.getStates().stream()
							.filter(state -> !nonPuzzleStates.contains(state.getId()))
							.allMatch(state -> puzzleManager.getPuzzleState(state.getId())
									.map(puzzleState -> puzzleState.equals("stopped"))
									.orElse(false));
					Thread.sleep(100);
					if(time % 1000 == 0) {
						log.info("checking...");
					}
					time += 100;
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			log.info("Passed init check");
			stateMachine.sendEvent("InitCheck_Signal");
		});
	}

	public Map<String,Puzzle> getPuzzles() {
		return puzzleManager.getPuzzles();
	}

	public Optional<String> getPuzzleState(String puzzleName) {
		return puzzleManager.getPuzzleState(puzzleName);
	}

	public Optional<String> getPuzzleStateInfo(String puzzleName) {
		return puzzleManager.getPuzzleStateInfo(puzzleName);
	}

	public Optional<DataObject> retrieveData(String groupname) {
		return dataManager.retrieveData(groupname);
	}

	public List<DataObject> retrieveData() {
		return dataManager.retrieveData();
	}

	public void storeData(String groupname) {
		dataManager.storeData(groupname, new ArrayList<>(puzzlesData.values()), timeRemaining.getRemainingTimeInSeconds(), succeeded);
	}

	@Value
	@Builder
	private static class Package {
		private String identifier;
		private String message;
	}

	private class StateMachineEventListener extends StateMachineListenerAdapter<String, String> {

		@Override
		public void stateChanged(State<String, String> from, State<String, String> to) {
		}

		@Override
		public void stateEntered(State<String, String> stateSM) {
			log.info("State Changed! - Current State: " + stateSM.getId());
			String state = stateSM.getId();
			switch (state) {
				case "InitCheck":
					log.info("Initializing all the puzzles, waiting for their response...");
					initPuzzles();
					checkInitPuzzles();
					break;
				case "Idle":
					log.info("Waiting for user input in order to start the puzzles");
					break;
				case "Finalize":
					log.info("Finalizing statemachine");
					succeeded = true;
					endSM();
					stateMachine.sendEvent("Finalize_Signal");
					break;
			}
		}

		@Override
		public void stateMachineStopped(StateMachine<String, String> stateMachine) {
			started = false;
			succeeded = false;
		}

	}

	private void endSM() {
		log.info("Finalizing room");
		EXECUTOR.shutdownNow();
		log.info("Stop all audio");
		audioManager.intializeAllMusic();
		log.info("Disconnecting serial port");
		disconnect();
		log.info("Stop timer");
		timeRemaining.pause();
		finished = true;
		started = false;
	}

}
