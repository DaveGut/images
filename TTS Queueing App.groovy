/*
TTS Queueing Application, Version 1

	Copyright 2019 Dave Gutheinz

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this  file except in compliance with the
License. You may obtain a copy of the License at: http://www.apache.org/licenses/LICENSE-2.0.
Unless required by applicable law or agreed to in writing,software distributed under the License is distributed on an 
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific 
language governing permissions and limitations under the License.

Description:  This application installs a virtual speaker that can be used as the TTS target in speech synthesis rules.
It has been tested against the Samsaung Multiroom Audio devices by Dave Gutheinz.  It should work with all speakers
having the Speech Synthesis capability (and audio notification).  However, because the recovery from a audio notification
is specific to the speaker driver, it is not guaranteed to work with other devices.

===== History =====
03.04.19	Initial release of beta version of TTS Queing Application
*/
	def appVersion() { return "0.5.01" }
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

def setLevel(level) { speaker.setLevel(level) }

def playTTS(playItem, volume, method) {
	logDebug("playTTS: playint: ${playItem}, volume = ${volume}, method = ${method}")
	def duration
	switch(method) {
		case "speak":
			speaker.speak(playItem)
			break
		case "playText":
			speaker.playText(playItem, volume)
			break
		case "playTextAndRestore":
			speaker.playTextAndRestore(playItem, volume)
			break
		case "playTextAndResume":
			speaker.playTextAndResume(playItem, volume)
			break
		case "playTrack":
			speaker.playText(playItem, volume)
			break
		case "playTrackAndRestore":
			speaker.playTextAndRestore(playItem, volume)
			break
		case "playTrackAndResume":
			speaker.playTextAndResume(playItem, volume)
			break
		default:
			return
	}
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
	def virtualDni = "${speaker.getDeviceNetworkId()}_TTS"
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

def setInitialStates() { }

def installed() {
	initialize()
}

def updated() { initialize() }

def initialize() {
	logDebug("initialize: speaker = ${speaker}")
	unsubscribe()
	unschedule()
	if (speaker) { addDevices() }
}

def uninstalled() {
    	getAllChildDevices().each { 
        deleteChildDevice(it.deviceNetworkId)
    }
}

def logDebug(msg){
	if(debugLog() == true) { log.debug msg }
}

//	end-of-file