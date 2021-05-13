/*
 [The "BSD licence"]
 Copyright (c) 2017 Kevin Jones
 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions
 are met:
 1. Redistributions of source code must retain the above copyright
    notice, this list of conditions and the following disclaimer.
 2. Redistributions in binary form must reproduce the above copyright
    notice, this list of conditions and the following disclaimer in the
    documentation and/or other materials provided with the distribution.
 3. The name of the author may not be used to endorse or promote products
    derived from this software without specific prior written permission.

 THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.nawforce.common.pkg

import com.nawforce.common.names.{Name, Names, TypeNames}
import com.nawforce.common.path.PathLike
import com.nawforce.common.{FileSystemHelper, TestHelper}
import org.scalatest.funsuite.AnyFunSuite

class DeleteTest extends AnyFunSuite with TestHelper {

  test("Valid delete") {
    withManualFlush {
      FileSystemHelper.run(Map("pkg/Foo.cls" -> "public class Foo {}")) { root: PathLike =>
        val path = root.join("pkg/Foo.cls")
        val org = createOrg(root)
        val pkg = org.unmanaged

        path.delete()
        pkg.refresh(path)
        assert(org.flush())

        assert(pkg.getTypeOfPath(path.toString) == null)
        assert(!org.issues.hasMessages)
      }
    }
  }

  test("Delete creates missing") {
    withManualFlush {
      FileSystemHelper.run(
        Map("pkg/Foo.cls" -> "public class Foo {Bar b;}", "pkg/Bar.cls" -> "public class Bar {}")) {
        root: PathLike =>
          val org = createOrg(root)
          val pkg = org.unmanaged
          assert(!org.issues.hasMessages)

          val path = root.join("pkg/Bar.cls")
          path.delete()
          pkg.refresh(path)
          assert(org.flush())

          assert(
            org.issues.getMessages("/pkg/Foo.cls")
              == "Missing: line 1 at 22-23: No type declaration found for 'Bar'\n")
      }
    }
  }

  test("Trigger valid delete") {
    withManualFlush {
      FileSystemHelper.run(Map("pkg/Foo.trigger" -> "trigger Foo on Account (before insert) {}")) {
        root: PathLike =>
          val org = createOrg(root)
          val pkg = org.unmanaged

          val path = root.join("pkg/Foo.trigger")
          path.delete()
          pkg.refresh(path)
          assert(org.flush())

          val fooTypeId = pkg.getTypeOfPath(root.join("pkg/Foo.trigger").toString)
          assert(pkg.getPathsOfType(fooTypeId).isEmpty)
          assert(!org.issues.hasMessages)
      }
    }
  }

  test("Delete creates trigger missing") {
    withManualFlush {
      FileSystemHelper.run(
        Map("pkg/Foo.trigger" -> "trigger Foo on Account (before insert) {Bar b;}",
            "pkg/Bar.cls" -> "public class Bar {}")) { root: PathLike =>
        val org = createOrg(root)
        val pkg = org.unmanaged
        assert(!org.issues.hasMessages)

        val path = root.join("pkg/Bar.cls")
        path.delete()
        pkg.refresh(path)
        assert(org.flush())
        assert(
          org.issues.getMessages("/pkg/Foo.trigger")
            == "Missing: line 1 at 44-45: No type declaration found for 'Bar'\n")
      }
    }
  }

  test("Delete label file") {
    withManualFlush {
      FileSystemHelper.run(
        Map("CustomLabels.labels" ->
          """<?xml version="1.0" encoding="UTF-8"?>
            |<CustomLabels xmlns="http://soap.sforce.com/2006/04/metadata">
            |    <labels>
            |        <fullName>TestLabel</fullName>
            |        <protected>false</protected>
            |    </labels>
            |</CustomLabels>
            |""".stripMargin)) { root: PathLike =>
        val org = createOrg(root)
        val pkg = org.unmanaged
        assert(!org.issues.hasMessages)

        val path = root.join("CustomLabels.labels")
        path.delete()
        pkg.refresh(path)
        assert(org.flush())

        val labels = pkg.orderedModules.head.findModuleType(TypeNames.Label).get
        assert(labels.fields.isEmpty)
      }
    }
  }

  test("Delete label file (multiple files)") {
    withManualFlush {
      FileSystemHelper.run(
        Map("CustomLabels.labels" ->
              """<?xml version="1.0" encoding="UTF-8"?>
              |<CustomLabels xmlns="http://soap.sforce.com/2006/04/metadata">
              |    <labels>
              |        <fullName>TestLabel</fullName>
              |        <protected>false</protected>
              |    </labels>
              |</CustomLabels>
              |""".stripMargin,
            "Alt.labels" ->
              """<?xml version="1.0" encoding="UTF-8"?>
              |<CustomLabels xmlns="http://soap.sforce.com/2006/04/metadata">
              |    <labels>
              |        <fullName>TestLabel2</fullName>
              |        <protected>false</protected>
              |    </labels>
              |</CustomLabels>
              |""".stripMargin,
        )) { root: PathLike =>
        val org = createOrg(root)
        val pkg = org.unmanaged
        assert(!org.issues.hasMessages)

        val path = root.join("CustomLabels.labels")
        path.delete()
        pkg.refresh(path)
        assert(org.flush())

        val labels = pkg.orderedModules.head.findModuleType(TypeNames.Label).get
        assert(labels.fields.length == 1)
        assert(labels.fields.exists(_.name.value == "TestLabel2"))
      }
    }
  }

  test("Delete flow file") {
    withManualFlush {
      FileSystemHelper.run(Map("Test.flow-meta.xml" -> "")) { root: PathLike =>
        val org = createOrg(root)
        val pkg = org.unmanaged
        assert(!org.issues.hasMessages)

        val path = root.join("Test.flow-meta.xml")
        path.delete()
        pkg.refresh(path)
        assert(org.flush())
        assert(pkg.interviews.get.nestedTypes.isEmpty)
      }
    }
  }

  test("Delete flow file (multiple)") {
    withManualFlush {
      FileSystemHelper.run(Map("Test.flow-meta.xml" -> "", "Test2.flow-meta.xml" -> "")) {
        root: PathLike =>
          val org = createOrg(root)
          val pkg = org.unmanaged
          assert(!org.issues.hasMessages)

          val path = root.join("Test.flow-meta.xml")
          path.delete()
          pkg.refresh(path)
          assert(org.flush())
          assert(pkg.interviews.get.nestedTypes.map(_.name).toSet == Set(Name("Test2")))
      }
    }
  }

  test("Delete page file") {
    withManualFlush {
      FileSystemHelper.run(Map("TestPage.page" -> "")) { root: PathLike =>
        val org = createOrg(root)
        val pkg = org.unmanaged
        assert(!org.issues.hasMessages)

        val path = root.join("TestPage.page")
        path.delete()
        pkg.refresh(path)
        assert(org.flush())
        assert(pkg.pages.get.fields.isEmpty)
      }
    }
  }

  test("Delete page file (multiple)") {
    withManualFlush {
      FileSystemHelper.run(Map("Test.page" -> "", "Test2.page" -> "")) { root: PathLike =>
        val org = createOrg(root)
        val pkg = org.unmanaged
        assert(!org.issues.hasMessages)

        val path = root.join("Test.page")
        path.delete()
        pkg.refresh(path)
        assert(org.flush())
        assert(pkg.pages.get.fields.map(_.name).toSet == Set(Name("Test2")))
      }
    }
  }

  test("Delete component file") {
    withManualFlush {
      FileSystemHelper.run(Map("Test.component" -> "<apex:component/>")) { root: PathLike =>
        val org = createOrg(root)
        val pkg = org.unmanaged
        assert(!org.issues.hasMessages)

        val path = root.join("Test.component")
        path.delete()
        pkg.refresh(path)
        assert(org.flush())
        assert(
          pkg.components.get.nestedTypes.map(_.name).toSet == Set(Names.c,
                                                                  Names.Apex,
                                                                  Names.Chatter))
      }
    }
  }

  test("Delete component file (multiple)") {
    withManualFlush {
      FileSystemHelper.run(
        Map("Test.component" -> "<apex:component/>", "Test2.component" -> "<apex:component/>")) {
        root: PathLike =>
          val org = createOrg(root)
          val pkg = org.unmanaged
          assert(!org.issues.hasMessages)

          val path = root.join("Test.component")
          path.delete()
          pkg.refresh(path)
          assert(org.flush())
          assert(
            pkg.components.get.nestedTypes.map(_.name).toSet == Set(Name("Test2"),
                                                                    Names.c,
                                                                    Names.Apex,
                                                                    Names.Chatter))
      }
    }
  }
}
