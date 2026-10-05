// MODULE: hiltlib
// ENABLE_HILT_KSP
// DISABLE_METRO
// FILE: UpstreamHiltModule.kt
// Verifies that hints for modules from Hilt-processed upstream libraries, such as hilt-android,
// aren't duplicated in every module that has Hilt interop enabled. On Android, duplicate hint
// classes fail dex merging.
package test

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class UpstreamHiltModule {
  @Provides fun provideMessage(): String = "Hello from Hilt"
}

// MODULE: liba(hiltlib)
// ENABLE_HILT_INTEROP
// GENERATE_CONTRIBUTION_HINTS_IN_FIR
// FILE: A.kt
package test

@Inject class A

// MODULE: libb(hiltlib)
// ENABLE_HILT_INTEROP
// GENERATE_CONTRIBUTION_HINTS_IN_FIR
// FILE: B.kt
package test

@Inject class B

// MODULE: main(liba, libb, hiltlib)
// ENABLE_HILT_INTEROP
// GENERATE_CONTRIBUTION_HINTS_IN_FIR

import javax.inject.Singleton
import test.A
import test.B

@DependencyGraph(Singleton::class)
interface AppGraph {
  val message: String
  val a: A
  val b: B
}

fun box(): String {
  val graph = createGraph<AppGraph>()
  assertEquals("Hello from Hilt", graph.message)

  val hint = "metro/hints/TestUpstreamHiltModuleJavax_inject_SingletonKt.class"
  val copies =
    AppGraph::class.java.classLoader.getResources(hint).toList().filter { it.protocol == "file" }
  assertTrue(copies.size <= 1, "Expected at most one module to contain $hint but found: $copies")
  return "OK"
}
