/*

*/
//	def debugLog() { return false }
	def debugLog() { return true }
definition(
	name: "TTS Queueing",
	namespace: "davegut",
	author: "Dave Gutheinz",
	description: "Queue text-to-speech messages for sequential playback.",
	iconUrl: "",
	iconX2Url: ""
)

preferences {
	page(name: "mainPage", title: "Queue message for speaker playback", install: true, uninstall: true)
}

def mainPage() {
	dynamicPage(name: "mainPage") {
		section {
			paragraph "You may only select a single real device in this application"
		}
		section {
//			input "speaker", "capability.audioNotification", title: "On this Audio Notification capableSpeaker player", required: true
			input "speaker", "capability.speechSynthesis", title: "On this real Speech Synthesis capable Speaker player", required: true
        }
	}
}

def setLevel(level) {
	speaker.setLevel(level)
}

def inputTTS(playItem, volume, method) {
	logDebug("inputTTS: playItem = ${playItem}, volume = ${volume}, method = ${method}")
	def TTSQueue = state.TTSQueue
	TTSQueue << [playItem, volume, method]
	if (state.playingTTS == false) { processQueue() }
}

def processQueue() {
	logDebug("processQueue: TTSQueue = ${state.TTSQueue}")
	state.playingTTS = true
	def TTSQueue = state.TTSQueue
	if (TTSQueue.size() == 0) {
		state.playingTTS = false
		return
	}
	def nextTTS = TTSQueue[0]
	TTSQueue.remove(0)
	playTTS(nextTTS[0], nextTTS[1], nextTTS[2])
}

def playTTS(playItem, volume, method) {
	logDebug("playTTS: playint: ${playItem}, volume = ${volume}, method = ${method}")
	def duration
	switch(method) {
		case "speak":
			duration = Math.max(Math.round(playItem.length()/12),2)+3
			speaker.speak(playItem)
			break
		case "playText":
			duration = Math.max(Math.round(playItem.length()/12),2)+3
			speaker.playText(playItem, volume)
			break
		case "playTextAndRestore":
			duration = Math.max(Math.round(playItem.length()/12),2)+3
			speaker.playTextAndRestore(playItem, volume)
			break
		case "playTextAndResume":
			duration = Math.max(Math.round(playItem.length()/12),2)+3
			speaker.playTextAndResume(playItem, volume)
			break
		case "playTrack":
			try {
				duration = playItem.duration.toInteger()+3
			} catch (e) {
				duration = 20
			}
			speaker.playText(playItem, volume)
			break
		case "playTrackAndRestore":
			try {
				duration = playItem.duration.toInteger()+3
			} catch (e) {
				duration = 20
			}
			speaker.playTextAndRestore(playItem, volume)
			break
		case "playTrackAndResume":
			try {
				duration = playItem.duration.toInteger()+3
			} catch (e) {
				duration = 20
			}
			speaker.playTextAndResume(playItem, volume)
			break
		default:
			return
	}
	runIn(duration, processQueue)
}

def clearQueue() {
	state.TTSQueue = []
	state.playingTTS = false
	logDebug("clearQueue:  TTSQueue = ${state.TTSQueue}")
}

def setInitialStates() {
}

def installed() {
	state.playingTTS = false
	state.TTSQueue = []
	initialize()
}

def updated() { initialize() }

def initialize() {
	logDebug("initialize: speaker = ${speaker}")
	unsubscribe()
	unschedule()
	if (speaker) { addDevices() }
}

def addDevices() {
	logDebug("addDevices: speaker = ${speaker}")
	try { 
		hub = location.hubs[0] 
	} catch (error) { 
		log.error "Hub not detected.  You must have a hub to install this app."
		return
	}
	def hubId = hub.id
	def virtualDni = "${speaker.getDeviceNetworkId}_TTS"
	def isChild = getChildDevice(virtualDni)
	if (!isChild) {
		logDebug("addDevices: ${virtualDni} / ${hubId} / speaker = ${speaker.label}")
		addChildDevice(
			"davegut",
			"Virtual TTS Speaker",
			virtualDni,
			hubId, [
				"label" : "${speaker.label} TTS Queue",
				"name" : "Virtual TTS Speaker"]
		)
			log.info "Installed Button Driver named ${speaker.label} TTS Queue"
	}
}

def logDebug(msg){
	if(debugLog() == true) { log.debug msg }
}

//	end-of-file