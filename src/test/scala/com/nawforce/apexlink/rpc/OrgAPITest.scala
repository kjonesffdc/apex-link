/*
 Copyright (c) 2019 Kevin Jones, All rights reserved.
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
 */

package com.nawforce.apexlink.rpc

import com.nawforce.pkgforce.diagnostics.{Diagnostic, ERROR_CATEGORY, Issue, Location}
import com.nawforce.pkgforce.path.PathFactory
import org.scalatest.Assertion
import org.scalatest.funsuite.AsyncFunSuite

import scala.concurrent.Future

class OrgAPITest extends AsyncFunSuite {
  test("Identifier not empty") {
    val orgAPI = OrgAPI()
    for {
      result <- orgAPI.identifier()
      _ <- orgAPI.reset()
    } yield {
      assert(result.nonEmpty)
    }
  }

  test("Add package not bad directory") {
    val orgAPI = OrgAPI()
    for {
      result <- orgAPI.open("/silly")
      issues <- orgAPI.getIssues(includeWarnings = false, includeZombies = false)
      _ <- orgAPI.reset()
    } yield {
      assert(result.error.isEmpty)
      assert(
        issues.issues sameElements Array(
          Issue("/silly", Diagnostic(ERROR_CATEGORY, Location.empty, "No directory at /silly"))))
    }
  }

  test("Add package MDAPI directory") {
    val workspace = PathFactory("samples/synthetic/mdapi-test")
    val orgAPI = OrgAPI()
    for {
      result <- orgAPI.open(workspace.toString)
      issues <- orgAPI.getIssues(includeWarnings = false, includeZombies = false)
      _ <- orgAPI.reset()
    } yield {
      assert(result.error.isEmpty)
      assert(issues.issues.forall(_.diagnostic.category != ERROR_CATEGORY))
    }
  }

  test("Add package sfdx directory (relative)") {
    val orgAPI = OrgAPI()
    for {
      result <- orgAPI.open("samples/synthetic/sfdx-test")
      issues <- orgAPI.getIssues(includeWarnings = false, includeZombies = false)
      _ <- orgAPI.reset()
    } yield {
      assert(result.error.isEmpty && result.namespaces.sameElements(Array("")))
      assert(issues.issues.forall(_.diagnostic.category != ERROR_CATEGORY))
    }
  }

  test("Add package sfdx directory (absolute)") {
    val workspace = PathFactory("samples/synthetic/sfdx-test")
    val orgAPI = OrgAPI()
    for {
      result <- orgAPI.open(workspace.toString)
      issues <- orgAPI.getIssues(includeWarnings = false, includeZombies = false)
      _ <- orgAPI.reset()
    } yield {
      assert(result.error.isEmpty && result.namespaces.sameElements(Array("")))
      assert(issues.issues.forall(_.diagnostic.category != ERROR_CATEGORY))
    }
  }

  test("Add package sfdx directory with ns (relative)") {
    val orgAPI = OrgAPI()
    for {
      result <- orgAPI.open("samples/synthetic/sfdx-ns-test")
      issues <- orgAPI.getIssues(includeWarnings = false, includeZombies = false)
      _ <- orgAPI.reset()
    } yield {
      assert(result.error.isEmpty && result.namespaces.sameElements(Array("sfdx_test", "")))
      assert(!issues.issues.exists(_.diagnostic.category == ERROR_CATEGORY))
    }
  }

  test("Add package sfdx directory with ns (absolute)") {
    val workspace = PathFactory("samples/synthetic/sfdx-ns-test")
    val orgAPI = OrgAPI()
    for {
      result <- orgAPI.open(workspace.toString)
      issues <- orgAPI.getIssues(includeWarnings = false, includeZombies = false)
      _ <- orgAPI.reset()
    } yield {
      assert(result.error.isEmpty && result.namespaces.sameElements(Array("sfdx_test", "")))
      assert(!issues.issues.exists(_.diagnostic.category == ERROR_CATEGORY))
    }
  }

  test("Get Issues") {
    val workspace = PathFactory("samples/synthetic/sfdx-ns-test")
    val orgAPI = OrgAPI()

    val pkg: Future[Assertion] = orgAPI.open(workspace.toString) map { result =>
      assert(result.error.isEmpty && result.namespaces.sameElements(Array("sfdx_test", "")))
    }

    val issues: Future[Assertion] = pkg flatMap { _ =>
      orgAPI.getIssues(includeWarnings = true, includeZombies = true) map { issuesResult =>
        orgAPI.reset()
        assert(issuesResult.issues.length == 3)
        assert(issuesResult.issues.count(_.path.contains("SingleError")) == 1)
        assert(issuesResult.issues.count(_.path.contains("DoubleError")) == 2)
      }
    }

    issues
  }
}