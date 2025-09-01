package com.termilu.hotbarautorefill;

import net.fabricmc.api.ClientModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HotbarAutoRefillClient implements ClientModInitializer {
	public static final String MOD_ID = "hotbar-auto-refill";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	@Override
	public void onInitializeClient() {
		// This entrypoint is suitable for setting up client-specific logic, such as rendering.
		LOGGER.info("Hello Fabric world from the client!");
	}
}