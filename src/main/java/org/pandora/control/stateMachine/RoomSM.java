package org.pandora.control.stateMachine;

import gnu.io.SerialPortEvent;
import lombok.Builder;
import lombok.Data;
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
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.PriorityQueue;
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
	private Integer duration;
	private Boolean succeeded = false;

	@Autowired
	public RoomSM(CountDown timeRemaining, HintManager hintManager, AudioManager audioManager, PuzzleManager puzzleManager, DataManager dataManager) throws Exception{
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
			EXECUTOR = Executors.newFixedThreadPool(10);
			checkForSerialPorts();
			stateMachine.stop();
			stateMachine.start();
			bufferIncoming = new StringBuilder();
			queue = new PriorityQueue();
			reset = true;
		} catch (Exception e) {
			log.error(String.format("Something went wrong initializing statemachine - %s", e.getMessage()));
		}
	}

	public void startPuzzles() {
		if(started) {
			stateMachine.sendEvent("START");
		}
	}

	public void startSM(String port) {
		if(reset) {
			duration = timeRemaining.getElapsedTime();
			initializePort(port);
			stateMachine.start();
			initPuzzles();
			checkInitPuzzles();
			finished = false;
			started = true;
			reset = false;
		}
	}

	public void stopSM() {
		stateMachine.stop();
		started = false;
		succeeded = false;
	}

	public void finishSM() {
		finishPuzzles();
		stateMachine.stop();
		succeeded = false;
		endSM();
	}

	public String getState() {
		return stateMachine.getState().getId();
	}

	public void setPuzzleState(String puzzleName, String state) {
		sendCommandToPuzzle(puzzleName, state);
		if (state.equals("finish") && puzzleManager.isPresent(puzzleName)) {
			puzzlesData.put(puzzleName, PuzzleData.builder().succeeded(false).build());
		}
	}

	private void finishPuzzle(String puzzleName) {
		PuzzleData.PuzzleDataBuilder puzzleData = PuzzleData.builder()
				.name(puzzleName)
				.duration(duration)
				.hints(hintManager.getNumberOfHints(puzzleName));
//		duration = timeRemaining.getElapsedTime() - duration; asd is not right with splitting states
		if (!puzzlesData.containsKey(puzzleName)) {
			puzzleData.succeeded(true);
		}
		puzzlesData.put(puzzleName, puzzleData.build());
	}

	private Action<String, String> finalizeSM() {
		succeeded = true;
		return endSM();
	}

	private Action<String, String> endSM() {
		return ctx -> {
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
		};
	}

	private Action<String, String> startRoom() {
		return ctx -> {
			log.info("Starting timer");
			timeRemaining.start();
			log.info("Starting audio");
			audioManager.playMusic("backgroundMusic");
		};
	}

	@Override
	public void serialEvent(SerialPortEvent evt) {
		if (evt.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
			log.info("RECEIVED DATA");
			try {
				bufferIncoming.append(getInput().read());
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
			}
		}
		bufferIncoming.delete(0, length);
		processQueue();
	}

	private void processQueue() {
		Package aPackage;
		while ((aPackage = queue.poll()) != null) {
			log.info(aPackage.getIdentifier() + aPackage.getMessage());
			applyPuzzle_PC(aPackage.getIdentifier(), aPackage.getMessage());
		}
	}

	@Value
	@Builder
	private static class Package {
		private String identifier;
		private String message;
	}

	private Future applyPuzzle_PC(String identifier, String message) {
		return EXECUTOR.submit(() -> puzzleManager.applyToPuzzle(identifier, message)
				.ifPresent(pair -> {
					if(pair.getValue1().equals("finished")) {
						stateMachine.sendEvent(String.format("%s_Event", pair.getValue0()));
						finishPuzzle(pair.getValue0());
					}
				}));
	}

	private Future initPuzzles() {
		return EXECUTOR.submit(() ->sendCommandToAllPuzzles("reset"));
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
			while(!init) {
				try {
					stateMachine.getStates().stream()
							.filter(state -> !state.getId().toLowerCase().contains("final"))
							.forEach(state->{
								log.info(state.getId());
								puzzleManager.getPuzzleState(state.getId()).ifPresent(log::info);
							});
					init = stateMachine.getStates().stream()
							.filter(state -> !state.getId().toLowerCase().contains("final"))
							.allMatch(state -> puzzleManager.getPuzzleState(state.getId())
									.map(puzzleState -> puzzleState.equals("stopped"))
									.orElse(false));
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			stateMachine.sendEvent("INITCHECK");
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

}
