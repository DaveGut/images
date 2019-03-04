/*
Virtural TTS Speaker Device Driver, Version 1

	Copyright 2019 Dave Gutheinz

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this  file except in compliance with the
License. You may obtain a copy of the License at: http://www.apache.org/licenses/LICENSE-2.0.
Unless required by applicable law or agreed to in writing,software distributed under the License is distributed on an 
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific 
language governing permissions and limitations under the License.

Description:  This driver is for a virtual device created by the app "TTS Queueing".  The virtual device provides the
framework to capture the external audio notification and then send it to the application for buffering and passing to the
speaker.
===== History =====
03.09.19	0.5.00	Initial release.  Moved buffer to virtual device handler to simplify application.  Single device
					only.
*/
def driverVer() {return "0.5.05" }
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
	input name: "msgDelay", type: "num", title: "Delay between buffered messages in seconds.", required: false
	input name: "testText", type: "text", title: "Enter desired test text.", reequired: false
	input name: "debugMode", type: "bool", title: "Display debug messages?", required: false
}

def installed() {
	log.info "Installing ${device.label}......"
	if (!msgDelay) { device.updateSetting("msgDelay", [type: "num", value: 10]) }
	if (!testText) { device.updateSetting("testText", [type: "text", value: "This is a test of Audio Notification Buffering"]) }
	if (!debugMode) { device.updateSetting("debugMode", [type:"bool", value: true]) }
	updateDataValue("driverVersion", driverVer())
	state.playingTTS = false
	state.TTSQueue = []
	runIn(1800, stopDebugLogging)
	runIn(2, updated)
}

def updated() {
	log.info "Updating ${device.label}......"
	unschedule()
	if (debugMode == true) { runIn(1800, stopDebugLogging) }
	else { stopDebugLogging() }
}

def stopDebugLogging() {
	log.trace "stopTraceLogging: Trace Logging is off."
	device.updateSetting("debugMode", [type:"bool", value: false])
}

def setLevel(level) {
	logDebug("setlevel: level = ${level}")
	parent.setLevel(level.toInteger())
}

def speak(text) {
	logDebug("speak: text = ${text}")
	def duration = textToSpeech(text).duration + msgDelay.toInteger()
	queueTTS(text, null, "speak", duration)
}

def playText(text, volume=null) {
	logDebug("playText: text = ${text}, volume = ${volume}")
	def duration = textToSpeech(text).duration + msgDelay.toInteger()
	queueTTS(text, volume, "playText", duration)
}

def playTextAndRestore(text, volume=null) {
	logDebug("playTextAndRestore: text = ${text}, volume = ${volume}")
	def duration = textToSpeech(text).duration + msgDelay.toInteger()
	queueTTS(text, volume, "playTextAndRestore", duration)
}

def playTextAndResume(text, volume=null) {
	logDebug("playText: text = ${text}, volume = ${volume}")
	def duration = textToSpeech(text).duration + msgDelay.toInteger()
	queueTTS(text, volume=null, "playTextAndResume", duration)
}

def playTrack(trackUri, volume=null) {
	logDebug("playTrack: text = ${text}, volume = ${volume}")
	def duration
	try { duration = playItem.duration.toInteger() + msgDelay.toInteger() }
	catch (e) { duration = 15 + msgDelay.toInteger }
	queueTTS(track, volume, "playTrack", duration)
}

def playTrackAndRestore(track, volume=null) {
	logDebug("playTrackAndRestore: text = ${text}, volume = ${volume}")
	def duration
	try { duration = playItem.duration.toInteger() + msgDelay.toInteger() }
	catch (e) { duration = 15 + msgDelay.toInteger }
	queueTTS(track, volume, "playTrack", duration)
}

def playTrackAndResume(track, volume=null) {
	logDebug("playTrackAndResume: text = ${text}, volume = ${volume}")
	def duration
	try { duration = playItem.duration.toInteger() + msgDelay.toInteger() }
	catch (e) { duration = 15 + msgDelay.toInteger }
	queueTTS(track, volume, "playTrack", duration)
}

def clearQueue() {
	state.TTSQueue = []
	state.playingTTS = false
	logDebug("clearQueue:  TTSQueue = ${state.TTSQueue}")
}

def testQueue() {
	speak("${testText} try one")
	pauseExecution(300)
	playText("${testText} try two", "44")
	pauseExecution(300)
	playTextAndRestore("${testText} try three", "33")
	pauseExecution(300)
	playTextAndResume("${testText} try four.  All done.", "44")
}

def logDebug(msg) {
	if (debugMode == true) {
		log.debug "${device.label} ${driverVer()}: ${msg}"
	}
}

def queueTTS(playItem, volume, method, duration) {
	logDebug("queueTTS: playItem = ${playItem}, volume = ${volume}, method = ${method}")
	def TTSQueue = state.TTSQueue
	TTSQueue << [playItem, volume, method, duration]
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
	parent.playTTS(nextTTS[0], nextTTS[1], nextTTS[2])
	runIn(nextTTS[3], processQueue)
}

//	End-of-File