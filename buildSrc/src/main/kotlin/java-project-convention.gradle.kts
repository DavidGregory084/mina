plugins {
    java
    `ivy-publish`
    jacoco
    id("com.github.ben-manes.versions")
}

repositories {
    mavenCentral()
}

group = "org.mina-lang"
version = "0.1.0-SNAPSHOT"

val junitVersion = "5.8.2"
val hamcrestVersion = "2.2"
val jacocoVersion = "0.8.8"

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter:${junitVersion}")
    testImplementation("org.hamcrest:hamcrest:${hamcrestVersion}")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}

jacoco {
    toolVersion = "${jacocoVersion}"
}

tasks.test {
    finalizedBy(tasks.jacocoTestReport)
}

publishing {
    publications {
        create<IvyPublication>("ivyLocal") {
            from(components["java"])
        }
    }
    repositories {
        ivy {
            url = uri("${System.getProperty("user.home")}/.ivy2/local")
            layout("ivy")
        }
    }
}
