/*===== HUBITAT INTEGRATION VERSION ===========================
===== HUBITAT INTEGRATION VERSION ===========================*/
metadata {
	definition (name: "Virtual TTS Speaker", namespace: "davegut", author: "David Gutheinz") {
		capability "Audio Notification"
		capability "Speech Synthesis"
		command "setLevel", ["NUMBER"]
		command "clearQueue"
		command "testQueue"
	}
}

preferences {
//	===== Set response modes (to display log & debug messages) =====
	input name: "debugMode", type: "bool", title: "Display debug messages?", required: false
}

def setLevel(level) {
	logDebug("setlevel: level = ${level}")
	parent.setLevel(level.toInteger())
}

def speak(text) {
	logDebug("speak: text = ${text}")
	parent.inputTTS(text, null, "speak")
}

def playText(text, volume=null) {
	logDebug("playText: text = ${text}, volume = ${volume}")
	parent.inputTTS(text, volume, "playText")
}

def playTextAndRestore(text, volume=null) {
	logDebug("playTextAndRestore: text = ${text}, volume = ${volume}")
	parent.inputTTS(text, volume, "playTextAndRestore")
}

def playTextAndResume(text, volume=null) {
	logDebug("playText: text = ${text}, volume = ${volume}")
	parent.inputTTS(text, volume, "playTextAndResume")
}

def playTrack(trackUri, volume=null) {
	logDebug("playTrack: text = ${text}, volume = ${volume}")
	parent.inputTTS(track, volume, "playTrack")
}

def playTrackAndRestore(track, volume=null) {
	logDebug("playTrackAndRestore: text = ${text}, volume = ${volume}")
	parent.inputTTS(track, volume, "playTrack")
}

def playTrackAndResume(track, volume=null) {
	logDebug("playTrackAndResume: text = ${text}, volume = ${volume}")
	parent.inputTTS(track, volume, "playTrack")
}

def clearQueue() { parent.clearQueue() }

def testQueue() {
	playTextAndResume("Hello David.  First Try", 30)
	pauseExecution(3000)
	playTextAndResume("Hello David.  Second Try", 50)
	pauseExecution(3000)
	playTextAndRestore("Hello David.  Third Try", 20)
	pauseExecution(3000)
	playTextAndRestore("Hello David.  Forth Try")
}
	

def logDebug(msg) {
	if (debugMode == true) {
		log.debug msg
	}
}
	
//	End-of-File