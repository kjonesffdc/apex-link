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

package com.nawforce.apexlink.org

import com.nawforce.apexlink.names._
import com.nawforce.apexlink.types.other._
import com.nawforce.apexlink.types.platform.PlatformTypeDeclaration
import com.nawforce.pkgforce.documents._
import com.nawforce.pkgforce.names.{EncodedName, Name, TypeName}

import scala.collection.mutable

class PackageImpl(val org: OrgImpl, val namespace: Option[Name], val basePackages: Seq[PackageImpl])
    extends PackageAPI {

  /** Modules used in this package, this will be null during construction. */
  var modules: Array[Module] = _

  /** Modules in construction phase. */
  private var moduleBuilder = new mutable.ArrayBuffer[Module]()

  /** Add a new module to the package, modules must be added in 'deploy order'. Once freezeModules is called no new
    * modules can be added. */
  private[nawforce] def add(module: Module): Unit = {
    assert(moduleBuilder != null)
    moduleBuilder.append(module)
  }

  /** Freeze the object post construction. */
  private[nawforce] def freeze(): Unit = {
    assert(modules == null)
    modules = moduleBuilder.toArray
    moduleBuilder = null

    modules.foreach(_.freeze())
  }

  /** Package modules in reverse deploy order, note ghost packages have no modules. */
  lazy val orderedModules: Seq[Module] = modules.reverse.toSeq

  /** Is this a ghost package, aka it has no modules. */
  lazy val isGhosted: Boolean = {
    modules.isEmpty
  }

  /** Is this or any base package of this a ghost package. */
  lazy val hasGhosted: Boolean = isGhosted || basePackages.exists(_.hasGhosted)

  /** Get summary of package context containing namespace & base package namespace information. */
  def packageContext: PackageContext = {
    val ghostedPackages = basePackages
      .groupBy(_.isGhosted)
      .map(kv => (kv._1, kv._2.map(_.namespace.map(_.value).getOrElse("")).sorted.toArray))
    PackageContext(namespace.map(_.value),
                   ghostedPackages.getOrElse(true, Array.empty),
                   ghostedPackages.getOrElse(false, Array.empty))
  }

  /** Set of namespaces used by this package and its base packages. */
  lazy val namespaces: Set[Name] = {
    namespace.toSet ++
      basePackages.flatMap(_.namespaces) ++
      PlatformTypeDeclaration.namespaces
  }

  /* Check if a type is ghosted in this package */
  def isGhostedType(typeName: TypeName): Boolean = {
    if (typeName.outer.contains(TypeNames.Schema)) {
      val encName = EncodedName(typeName.name)
      basePackages.filter(_.isGhosted).exists(_.namespace == encName.namespace)
    } else {
      basePackages.filter(_.isGhosted).exists(_.namespace.contains(typeName.outerName)) ||
      typeName.params.exists(isGhostedType)
    }
  }

  /** Check if a field name is ghosted in this package. */
  def isGhostedFieldName(name: Name): Boolean = {
    EncodedName(name).namespace match {
      case None     => false
      case Some(ns) => basePackages.filter(_.isGhosted).exists(_.namespace.contains(ns))
    }
  }

  /** Check all summary types have propagated their dependencies. */
  def propagateAllDependencies(): Unit = {
    modules.foreach(_.propagateAllDependencies())
  }
}
