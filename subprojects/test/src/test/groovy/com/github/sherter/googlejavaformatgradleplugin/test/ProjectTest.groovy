package com.github.sherter.googlejavaformatgradleplugin.test

import org.junit.jupiter.api.io.TempDir
import spock.lang.Specification

class ProjectTest extends Specification {

    @TempDir
    File rootDir
    Project project

    void setup() {
        project = new Project(rootDir)
    }

    def 'create file'() {
        when:
        project.createFile(['foo'])

        then:
        def file = new File(temporaryFolder.root, 'foo')
        file.exists()
        file.readBytes() == [] as byte[]
    }

    def 'create file with non-existing parent directories'() {
        when:
        project.createFile(['foo', 'bar', 'baz'])

        then:
        def file = new File(temporaryFolder.root, 'foo/bar/baz')
        file.exists()
        file.readBytes() == [] as byte[]
    }

    def 'create file with initial content'() {
        when:
        project.createFile(['foo'], [1, 2, 3] as byte[])

        then:
        def file = new File(temporaryFolder.root, 'foo')
        file.exists()
        file.readBytes() == [1, 2, 3] as byte[]
    }

    def 'create file with initial string as content'() {
        when:
        project.createFile(['foo'], 'bar')

        then:
        def file = new File(temporaryFolder.root, 'foo')
        file.exists()
        file.readLines('UTF-8') == ['bar']
    }

}
