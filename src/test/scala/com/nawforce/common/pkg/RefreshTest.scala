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

import com.nawforce.common.api.IssueOptions
import com.nawforce.common.names.{Name, Names, TypeNames}
import com.nawforce.common.org.PackageImpl
import com.nawforce.common.path.PathLike
import com.nawforce.common.{FileSystemHelper, TestHelper}
import org.scalatest.funsuite.AnyFunSuite

class RefreshTest extends AnyFunSuite with TestHelper {

  private def refresh(pkg: PackageImpl, path: PathLike, source: String): Unit = {
    path.write(source)
    pkg.refresh(path)
  }

  test("Valid refresh") {
    withManualFlush {
      FileSystemHelper.run(Map("pkg/Foo.cls" -> "public class Foo {}")) { root: PathLike =>
        val org = createOrg(root)
        val pkg = org.unmanaged
        refresh(pkg, root.join("pkg/Foo.cls"), "public class Foo {}")
        assert(org.flush())
        assert(!org.issues.hasMessages)
      }
    }
  }

  test("Valid refresh (new)") {
    withManualFlush {
      FileSystemHelper.run(Map()) { root: PathLike =>
        val org = createOrg(root)
        val pkg = org.unmanaged
        refresh(pkg, root.join("pkg").join("Foo.cls"), "public class Foo {}")
        assert(org.flush())
        assert(!org.issues.hasMessages)
        assert(pkg.getTypeOfPathInternal(root.join("pkg").join("Foo.cls")) != null)
      }
    }
  }

  test("Valid refresh with changes") {
    withManualFlush {
      FileSystemHelper.run(Map("pkg/Foo.cls" -> "public class Foo {}")) { root: PathLike =>
        val org = createOrg(root)
        val pkg = org.unmanaged
        refresh(pkg, root.join("pkg/Foo.cls"), "public class Foo {Object a;}")
        assert(org.flush())
        assert(!org.issues.hasMessages)
      }
    }
  }

  test("Valid refresh with non-significant changes") {
    withManualFlush {
      FileSystemHelper.run(Map("pkg/Foo.cls" -> "public class Foo {}")) { root: PathLike =>
        val org = createOrg(root)
        val pkg = org.unmanaged
        refresh(pkg, root.join("pkg/Foo.cls"), "public class Foo {/* A change */}")
        assert(org.flush())
        assert(!org.issues.hasMessages)
      }
    }
  }

  test("Refresh creates missing") {
    withManualFlush {
      FileSystemHelper.run(
        Map("pkg/Foo.cls" -> "public class Foo {Bar.Inner b;}",
            "pkg/Bar.cls" -> "public class Bar {public class Inner {}}")) { root: PathLike =>
        val org = createOrg(root)
        val pkg = org.unmanaged
        assert(!org.issues.hasMessages)

        refresh(pkg, root.join("pkg/Bar.cls"), "public class Bar {}")
        assert(org.flush())
        assert(
          org.issues.getMessages("/pkg/Foo.cls")
            == "Missing: line 1 at 28-29: No type declaration found for 'Bar.Inner'\n")
      }
    }
  }

  test("Refresh resolves missing") {
    withManualFlush {
      FileSystemHelper.run(
        Map("pkg/Foo.cls" -> "public class Foo {Bar.Inner b;}",
            "pkg/Bar.cls" -> "public class Bar {}")) { root: PathLike =>
        val org = createOrg(root)
        val pkg = org.unmanaged
        assert(
          org.issues.getMessages("/pkg/Foo.cls")
            == "Missing: line 1 at 28-29: No type declaration found for 'Bar.Inner'\n")

        refresh(pkg, root.join("pkg/Bar.cls"), "public class Bar {public class Inner {}}")
        assert(org.flush())
        assert(!org.issues.hasMessages)
      }
    }
  }

  test("Dependencies created") {
    withManualFlush {
      FileSystemHelper.run(
        Map("pkg/Foo.cls" -> "public class Foo {}", "pkg/Bar.cls" -> "public class Bar {}")) {
        root: PathLike =>
          val org = createOrg(root)
          val pkg = org.unmanaged
          refresh(pkg, root.join("pkg/Foo.cls"), "public class Foo {Bar b;}")
          assert(org.flush())
          assert(!org.issues.hasMessages)

          val fooTypeId =
            pkg.getTypeOfPathInternal(root.join("pkg").join("Foo.cls")).get.asTypeIdentifier
          val barTypeId =
            pkg.getTypeOfPathInternal(root.join("pkg").join("Bar.cls")).get.asTypeIdentifier

          assert(pkg.getDependencyHolders(fooTypeId).isEmpty)
          assert(
            pkg
              .getDependencies(fooTypeId, outerInheritanceOnly = false)
              .sameElements(Array(barTypeId)))

          assert(pkg.getDependencyHolders(barTypeId).sameElements(Array(fooTypeId)))
          assert(pkg.getDependencies(barTypeId, outerInheritanceOnly = false).isEmpty)
      }
    }
  }

  test("Dependencies created cross package") {
    withManualFlush {
      FileSystemHelper.run(
        Map("sfdx-project.json" ->
              """{
              |"namespace": "pkg2",
              |"packageDirectories": [{"path": "pkg2"}],
              |"plugins": {"dependencies": [{"namespace": "pkg1", "path": "pkg1"}]}
              |}""".stripMargin,
            "pkg1/Bar.cls" -> "global class Bar {}",
            "pkg2/Foo.cls" -> "public class Foo {}")) { root: PathLike =>
        val org = createOrg(root)
        val pkg1 = org.packagesByNamespace(Some(Name("pkg1")))
        val pkg2 = org.packagesByNamespace(Some(Name("pkg2")))
        refresh(pkg2, root.join("pkg2/Foo.cls"), "public class Foo {pkg1.Bar b;}")
        assert(org.flush())
        assert(!org.issues.hasMessages)

        val barTypeId =
          pkg1.getTypeOfPathInternal(root.join("pkg1").join("Bar.cls")).get.asTypeIdentifier
        val fooTypeId =
          pkg2.getTypeOfPathInternal(root.join("pkg2").join("Foo.cls")).get.asTypeIdentifier

        assert(pkg2.getDependencyHolders(fooTypeId).isEmpty)
        assert(
          pkg2
            .getDependencies(fooTypeId, outerInheritanceOnly = false)
            .sameElements(Array(barTypeId)))

        assert(pkg1.getDependencyHolders(barTypeId).sameElements(Array(fooTypeId)))
        assert(pkg1.getDependencies(barTypeId, outerInheritanceOnly = false).isEmpty)
      }
    }
  }

  test("Valid trigger refresh") {
    withManualFlush {
      FileSystemHelper.run(Map("pkg/Foo.trigger" -> "trigger Foo on Account (before insert) {}")) {
        root: PathLike =>
          val org = createOrg(root)
          val pkg = org.unmanaged
          refresh(pkg, root.join("pkg/Foo.trigger"), "trigger Foo on Account (before insert) {}")
          assert(org.flush())
          assert(!org.issues.hasMessages)
      }
    }
  }

  test("Valid trigger refresh (new)") {
    withManualFlush {
      FileSystemHelper.run(Map()) { root: PathLike =>
        val org = createOrg(root)
        val pkg = org.unmanaged
        refresh(pkg, root.join("pkg/Foo.trigger"), "trigger Foo on Account (before insert) {}")
        assert(org.flush())
        assert(!org.issues.hasMessages)
        assert(pkg.getTypeOfPathInternal(root.join("pkg").join("Foo.trigger")) != null)
      }
    }
  }

  test("Valid trigger refresh with changes") {
    withManualFlush {
      FileSystemHelper.run(Map("pkg/Foo.trigger" -> "trigger Foo on Account (before insert) {}")) {
        root: PathLike =>
          val org = createOrg(root)
          val pkg = org.unmanaged
          refresh(pkg,
                  root.join("pkg/Foo.trigger"),
                  "trigger Foo on Account (before insert) {Object a;}")
          assert(org.flush())
          assert(!org.issues.hasMessages)
      }
    }
  }

  test("Refresh creates trigger missing") {
    withManualFlush {
      FileSystemHelper.run(
        Map("pkg/Foo.trigger" -> "trigger Foo on Account (before insert) {Bar.Inner b;}",
            "pkg/Bar.cls" -> "public class Bar {public class Inner {}}")) { root: PathLike =>
        val org = createOrg(root)
        val pkg = org.unmanaged
        assert(!org.issues.hasMessages)

        refresh(pkg, root.join("pkg/Bar.cls"), "public class Bar {}")
        assert(org.flush())
        assert(
          org.issues.getMessages("/pkg/Foo.trigger")
            == "Missing: line 1 at 50-51: No type declaration found for 'Bar.Inner'\n")
      }
    }
  }

  test("Refresh resolves trigger missing") {
    withManualFlush {
      FileSystemHelper.run(
        Map("pkg/Foo.trigger" -> "trigger Foo on Account (before insert) {Bar.Inner b;}",
            "pkg/Bar.cls" -> "public class Bar {}")) { root: PathLike =>
        val org = createOrg(root)
        val pkg = org.unmanaged
        assert(
          org.issues.getMessages("/pkg/Foo.trigger")
            == "Missing: line 1 at 50-51: No type declaration found for 'Bar.Inner'\n")

        refresh(pkg, root.join("pkg/Bar.cls"), "public class Bar {public class Inner {}}")
        assert(org.flush())
        assert(!org.issues.hasMessages)
      }
    }
  }

  test("Trigger dependencies created") {
    withManualFlush {
      FileSystemHelper.run(
        Map("pkg/Foo.trigger" -> "trigger Foo on Account (before insert) {}",
            "pkg/Bar.cls" -> "public class Bar {}")) { root: PathLike =>
        val org = createOrg(root)
        val pkg = org.unmanaged
        refresh(pkg,
                root.join("pkg/Foo.trigger"),
                "trigger Foo on Account (before insert) {Bar b;}")
        assert(org.flush())

        val fooTypeId =
          pkg.getTypeOfPathInternal(root.join("pkg").join("Foo.trigger")).get.asTypeIdentifier
        val barTypeId =
          pkg.getTypeOfPathInternal(root.join("pkg").join("Bar.cls")).get.asTypeIdentifier

        assert(pkg.getDependencyHolders(fooTypeId).isEmpty)
        assert(
          pkg
            .getDependencies(fooTypeId, outerInheritanceOnly = false)
            .sameElements(Array(barTypeId)))

        assert(pkg.getDependencyHolders(barTypeId).sameElements(Array(fooTypeId)))
        assert(pkg.getDependencies(barTypeId, outerInheritanceOnly = false).isEmpty)
      }
    }
  }

  test("Trigger dependencies created cross package") {
    withManualFlush {
      FileSystemHelper.run(
        Map("sfdx-project.json" ->
              """{
          |"namespace": "pkg2",
          |"packageDirectories": [{"path": "pkg2"}],
          |"plugins": {"dependencies": [{"namespace": "pkg1", "path": "pkg1"}]}
          |}""".stripMargin,
            "pkg1/Bar.cls" -> "global class Bar {}",
            "pkg2/Foo.trigger" -> "trigger Foo on Account (before insert) {}")) { root: PathLike =>
        val org = createOrg(root)
        val pkg1 = org.packagesByNamespace(Some(Name("pkg1")))
        val pkg2 = org.packagesByNamespace(Some(Name("pkg2")))
        refresh(pkg2,
                root.join("pkg2/Foo.trigger"),
                "trigger Foo on Account (before insert) {pkg1.Bar b;}")
        assert(org.flush())

        val fooTypeId =
          pkg2.getTypeOfPathInternal(root.join("pkg2").join("Foo.trigger")).get.asTypeIdentifier
        val barTypeId =
          pkg1.getTypeOfPathInternal(root.join("pkg1").join("Bar.cls")).get.asTypeIdentifier

        assert(pkg2.getDependencyHolders(fooTypeId).isEmpty)
        assert(
          pkg2
            .getDependencies(fooTypeId, outerInheritanceOnly = false)
            .sameElements(Array(barTypeId)))

        assert(pkg1.getDependencyHolders(barTypeId).sameElements(Array(fooTypeId)))
        assert(pkg1.getDependencies(barTypeId, outerInheritanceOnly = false).isEmpty)
      }
    }
  }

  test("Valid label upsert") {
    withManualFlush {
      FileSystemHelper.run(Map(
        "CustomLabels.labels" -> "<CustomLabels xmlns=\"http://soap.sforce.com/2006/04/metadata\"/>")) {
        root: PathLike =>
          val org = createOrg(root)
          val pkg = org.unmanaged
          assert(!org.issues.hasMessages)

          refresh(pkg,
                  root.join("CustomLabels.labels"),
                  "<CustomLabels xmlns=\"http://soap.sforce.com/2006/04/metadata\"/>")
          assert(org.flush())
          assert(!org.issues.hasMessages)
      }
    }
  }

  test("Valid label upsert (new)") {
    withManualFlush {
      FileSystemHelper.run(Map()) { root: PathLike =>
        val org = createOrg(root)
        val pkg = org.unmanaged
        assert(!org.issues.hasMessages)

        refresh(pkg,
                root.join("CustomLabels.labels"),
                "<CustomLabels xmlns=\"http://soap.sforce.com/2006/04/metadata\"/>")
        assert(org.flush())
        assert(!org.issues.hasMessages)
      }
    }
  }

  test("Valid label upsert (changed)") {
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

        refresh(pkg,
                root.join("CustomLabels.labels"),
                """<?xml version="1.0" encoding="UTF-8"?>
            |<CustomLabels xmlns="http://soap.sforce.com/2006/04/metadata">
            |    <labels>
            |        <fullName>TestLabel2</fullName>
            |        <protected>false</protected>
            |    </labels>
            |</CustomLabels>
            |""".stripMargin)
        assert(org.flush())
        val labels = pkg.orderedModules.head.findModuleType(TypeNames.Label).get
        assert(labels.fields.length == 1)
        assert(labels.fields.exists(_.name.value == "TestLabel2"))
      }
    }
  }

  test("Valid label upsert (alt file)") {
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

        refresh(pkg,
                root.join("Alt.labels"),
                """<?xml version="1.0" encoding="UTF-8"?>
            |<CustomLabels xmlns="http://soap.sforce.com/2006/04/metadata">
            |    <labels>
            |        <fullName>TestLabel2</fullName>
            |        <protected>false</protected>
            |    </labels>
            |</CustomLabels>
            |""".stripMargin)
        org.flush()
        val labels = pkg.orderedModules.head.findModuleType(TypeNames.Label).get
        assert(labels.fields.length == 2)
        assert(labels.fields.exists(_.name.value == "TestLabel"))
        assert(labels.fields.exists(_.name.value == "TestLabel2"))
      }
    }
  }

  test("Valid label class dependent") {
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
            "Dummy.cls" -> "public class Dummy { {String a = Label.TestLabel;}}")) {
        root: PathLike =>
          val org = createOrg(root)
          val pkg = org.unmanaged
          assert(!org.issues.hasMessages)

          refresh(pkg,
                  root.join("CustomLabels.labels"),
                  "<CustomLabels xmlns=\"http://soap.sforce.com/2006/04/metadata\"/>")
          assert(org.flush())
          assert(
            org.getIssues(new IssueOptions()) == "/Dummy.cls\nMissing: line 1 at 33-48: Unknown field or type 'TestLabel' on 'System.Label'\n")
      }
    }
  }

  test("Valid label class dependent (reversed)") {
    withManualFlush {
      FileSystemHelper.run(
        Map(
          "CustomLabels.labels" -> "<CustomLabels xmlns=\"http://soap.sforce.com/2006/04/metadata\"/>",
          "Dummy.cls" -> "public class Dummy { {String a = Label.TestLabel;}}")) { root: PathLike =>
        val org = createOrg(root)
        val pkg = org.unmanaged
        assert(
          org.getIssues(new IssueOptions()) == "/Dummy.cls\nMissing: line 1 at 33-48: Unknown field or type 'TestLabel' on 'System.Label'\n")

        refresh(pkg,
                root.join("CustomLabels.labels"),
                """<?xml version="1.0" encoding="UTF-8"?>
            |<CustomLabels xmlns="http://soap.sforce.com/2006/04/metadata">
            |    <labels>
            |        <fullName>TestLabel</fullName>
            |        <protected>false</protected>
            |    </labels>
            |</CustomLabels>
            |""".stripMargin)
        assert(org.flush())
        assert(!org.issues.hasMessages)
      }
    }
  }

  test("Valid flow upsert") {
    withManualFlush {
      FileSystemHelper.run(Map("Test.flow-meta.xml" -> "")) { root: PathLike =>
        val org = createOrg(root)
        val pkg = org.unmanaged
        assert(!org.issues.hasMessages)

        refresh(pkg, root.join("Test.flow-meta.xml"), "")
        assert(org.flush())
        assert(pkg.interviews.get.findNestedType(Name("Test")).nonEmpty)
        assert(!org.issues.hasMessages)
      }
    }
  }

  test("Valid flow upsert (new)") {
    withManualFlush {
      FileSystemHelper.run(Map()) { root: PathLike =>
        val org = createOrg(root)
        val pkg = org.unmanaged
        assert(!org.issues.hasMessages)

        refresh(pkg, root.join("Test.flow-meta.xml"), "")
        assert(org.flush())
        assert(pkg.interviews.get.findNestedType(Name("Test")).nonEmpty)
        assert(!org.issues.hasMessages)
      }
    }
  }

  test("Valid flow upsert (changed)") {
    withManualFlush {
      FileSystemHelper.run(Map("Test.flow-meta.xml" -> "")) { root: PathLike =>
        val org = createOrg(root)
        val pkg = org.unmanaged
        assert(!org.issues.hasMessages)

        refresh(pkg, root.join("Test.flow-meta.xml"), "Changed")
        assert(org.flush())
        assert(pkg.interviews.get.findNestedType(Name("Test")).nonEmpty)
      }
    }
  }

  test("Valid flow upsert (new flow)") {
    withManualFlush {
      FileSystemHelper.run(Map("Test.flow-meta.xml" -> "")) { root: PathLike =>
        val org = createOrg(root)
        val pkg = org.unmanaged
        assert(!org.issues.hasMessages)

        refresh(pkg, root.join("Test2.flow-meta.xml"), "")
        assert(org.flush())
        assert(pkg.interviews.get.nestedTypes.map(_.name).toSet == Set(Name("Test"), Name("Test2")))
      }
    }
  }

  test("Valid page upsert") {
    withManualFlush {
      FileSystemHelper.run(Map("TestPage.page" -> "")) { root: PathLike =>
        val org = createOrg(root)
        val pkg = org.unmanaged
        assert(!org.issues.hasMessages)

        refresh(pkg, root.join("TestPage.page"), "")
        assert(org.flush())
        assert(pkg.pages.get.findField(Name("TestPage"), Some(true)).nonEmpty)
      }
    }
  }

  test("Valid page upsert (new)") {
    withManualFlush {
      FileSystemHelper.run(Map()) { root: PathLike =>
        val org = createOrg(root)
        val pkg = org.unmanaged
        assert(!org.issues.hasMessages)

        refresh(pkg, root.join("TestPage.page"), "")
        assert(org.flush())
        assert(pkg.pages.get.findField(Name("TestPage"), Some(true)).nonEmpty)
      }
    }
  }

  test("Valid page upsert (changed)") {
    withManualFlush {
      FileSystemHelper.run(Map("TestPage.page" -> "")) { root: PathLike =>
        val org = createOrg(root)
        val pkg = org.unmanaged
        assert(!org.issues.hasMessages)

        refresh(pkg, root.join("TestPage.page"), " ")
        assert(org.flush())
        assert(pkg.pages.get.findField(Name("TestPage"), Some(true)).nonEmpty)
      }
    }
  }

  test("Valid page upsert (new page)") {
    withManualFlush {
      FileSystemHelper.run(Map("TestPage.page" -> "")) { root: PathLike =>
        val org = createOrg(root)
        val pkg = org.unmanaged
        assert(!org.issues.hasMessages)

        refresh(pkg, root.join("TestPage2.page"), " ")
        assert(org.flush())
        assert(pkg.pages.get.fields.map(_.name).toSet == Set(Name("TestPage"), Name("TestPage2")))
      }
    }
  }

  test("Valid component upsert") {
    withManualFlush {
      FileSystemHelper.run(Map("Test.component" -> "<apex:component/>")) { root: PathLike =>
        val org = createOrg(root)
        val pkg = org.unmanaged
        assert(!org.issues.hasMessages)

        refresh(pkg, root.join("Test.component"), "<apex:component/> ")
        assert(org.flush())
        assert(pkg.components.get.findNestedType(Name("Test")).nonEmpty)
      }
    }
  }

  test("Valid component upsert (new)") {
    withManualFlush {
      FileSystemHelper.run(Map()) { root: PathLike =>
        val org = createOrg(root)
        val pkg = org.unmanaged
        assert(!org.issues.hasMessages)

        refresh(pkg, root.join("Test.component"), "<apex:component/>")
        assert(org.flush())
        assert(pkg.components.get.findNestedType(Name("Test")).nonEmpty)
      }
    }
  }

  test("Valid component upsert (changed)") {
    withManualFlush {
      FileSystemHelper.run(Map("Test.component" -> "<apex:component/>")) { root: PathLike =>
        val org = createOrg(root)
        val pkg = org.unmanaged
        assert(!org.issues.hasMessages)

        refresh(pkg, root.join("Test.component"), "<apex:component/> ")
        assert(org.flush())
        assert(pkg.components.get.findNestedType(Name("Test")).nonEmpty)
      }
    }
  }

  test("Valid component upsert (new component)") {
    withManualFlush {
      FileSystemHelper.run(Map("Test.component" -> "<apex:component/>")) { root: PathLike =>
        val org = createOrg(root)
        val pkg = org.unmanaged
        assert(!org.issues.hasMessages)

        refresh(pkg, root.join("Test2.component"), "<apex:component/> ")
        assert(org.flush())
        assert(
          pkg.components.get.nestedTypes.map(_.name).toSet ==
            Set(Name("Test"), Name("Test2"), Names.c, Names.Apex, Names.Chatter))
      }
    }
  }

  test("Deferred issue reporting") {
    withManualFlush {
      FileSystemHelper.run(Map("Dummy.cls" -> "public class Dummy {}")) { root: PathLike =>
        val org = createOrg(root)
        val pkg = org.unmanaged
        assert(!org.issues.hasMessages)

        refresh(pkg, root.join("Dummy.cls"), "public class Dummy {")
        assert(!org.issues.hasMessages)

        assert(org.flush())
        assert(
          org.issues
            .getMessages("/Dummy.cls")
            .startsWith("Syntax: line 1 at 20: mismatched input '<EOF>' expecting {"))
      }
    }
  }
}
