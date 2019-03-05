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
03.04.19	0.5.00	Initial release.  Moved buffer to virtual device handler to simplify application.  Single device
					only.
03.05.19	0.6.01	Updated to support multi-device app.  Also limited commands to setLevel and speak.
*/
def driverVer() {return "0.5.05" }
metadata {
	definition (name: "Virtual TTS Speaker", namespace: "davegut", author: "David Gutheinz") {
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

void uninstalled() {
	try {
		def alias = device.label
		log.info "Removing device ${alias} with DNI = ${device.deviceNetworkId}"
		parent.removeChildDevice(alias, device.deviceNetworkId)
	} catch (ex) {
		log.info "${device.name} ${device.label}: Either the device was manually installed or there was an error."
	}
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
	queueTTS(text.toString(), duration)
}

def clearQueue() {
	state.TTSQueue = []
	state.playingTTS = false
	logDebug("clearQueue:  TTSQueue = ${state.TTSQueue}")
}

def testQueue() {
	speak("${testText} try one")
	pauseExecution(1000)
	speak("${testText} try two")
	pauseExecution(200)
	speak("${testText} try three")
	pauseExecution(200)
	speak("${testText} try four.  All done.")
}

def logDebug(msg) {
	if (debugMode == true) {
		log.debug "${device.label} ${driverVer()}: ${msg}"
	}
}

def queueTTS(playItem, duration) {
	logDebug("queueTTS: playItem = ${playItem}, duration = ${duration}")
	def TTSQueue = state.TTSQueue
	TTSQueue << [playItem, duration]
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
	def realSpeaker = getDataValue("realSpeaker")
	def nextTTS = TTSQueue[0]
	TTSQueue.remove(0)
	parent.playTTS(nextTTS[0], realSpeaker)
	runIn(nextTTS[1], processQueue)
}

//	End-of-File