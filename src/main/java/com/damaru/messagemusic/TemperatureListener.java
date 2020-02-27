package com.damaru.messagemusic;

import com.damaru.messagemusic.midi.MusicPlayer;
import com.solace.temperature.Temperature;
import com.solace.temperature.TemperatureMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
class TemperatureListener implements TemperatureMessage.SubscribeListener {

	private static final Logger log = LoggerFactory.getLogger(TemperatureListener.class);

	@Autowired
	MusicPlayer musicPlayer;

	@Override
	public void onReceive(TemperatureMessage temperatureMessage) {
		Temperature temperature = temperatureMessage.getPayload();
		log.debug("temp: " + temperature.getSensorId() + ": " + temperature.getTemperature());
		musicPlayer.setValue(temperature.getSensorId(), temperature.getTemperature());
	}

	@Override
	public void handleException(Exception e) {
		log.error("Error: ", e);
	}
}
