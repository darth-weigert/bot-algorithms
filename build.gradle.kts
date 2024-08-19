import korlibs.korge.gradle.*

plugins {
	alias(libs.plugins.korge)
}

korge {
	id = "br.darthweigert.algorithms"

// To enable all targets at once

	//targetAll()

// To enable targets based on properties/environment variables
	//targetDefault()

// To selectively enable targets
	
	targetJvm()
	targetJs()
    targetWasmJs()
//	targetDesktop()
	targetIos()
	targetAndroid()

//	serializationJson()
}


dependencies {
    add("commonMainApi", project(":deps"))
    add("commonMainApi", "org.jetbrains.kotlinx:kotlinx-collections-immutable:0.3.7")
//    add("commonTestApi", "io.kotest:kotest-assertions-core:5.7.2")
    add("jvmTestApi", "io.kotest:kotest-assertions-core:5.7.2")
}

