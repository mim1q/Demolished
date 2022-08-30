package com.github.mim1q.demolished

import net.fabricmc.api.ModInitializer
import net.minecraft.util.Identifier
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

object Demolished : ModInitializer {
  const val MOD_ID = "demolished"
  val LOGGER: Logger = LogManager.getLogger()

  override fun onInitialize() {
    LOGGER.info("$MOD_ID initialization started")
  }

  fun createId(path: String): Identifier {
    return Identifier(MOD_ID, path)
  }
}