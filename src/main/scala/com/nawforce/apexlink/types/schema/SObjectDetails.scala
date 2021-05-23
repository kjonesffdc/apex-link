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

package com.nawforce.apexlink.types.schema

import com.nawforce.apexlink.names.TypeNames
import com.nawforce.apexlink.org.{Module, OrgImpl}
import com.nawforce.apexlink.types.synthetic.CustomFieldDeclaration
import com.nawforce.pkgforce.diagnostics.{Location, PathLocation}
import com.nawforce.pkgforce.documents._
import com.nawforce.pkgforce.names.{EncodedName, Name, TypeName}
import com.nawforce.pkgforce.path.PathLike
import com.nawforce.pkgforce.xml.{XMLElementLike, XMLException, XMLFactory}

sealed abstract class SObjectNature(val nature: String) {
  override def toString: String = nature
}
abstract class IntroducingNature(_nature: String) extends SObjectNature(_nature)
case object ListCustomSettingNature extends IntroducingNature("List")
case object HierarchyCustomSettingsNature extends IntroducingNature("Hierarchy")
case object CustomObjectNature extends IntroducingNature("CustomObject")
case object CustomMetadataNature extends SObjectNature("CustomMetadata")
case object BigObjectNature extends SObjectNature("BigObject")
case object PlatformObjectNature extends SObjectNature("PlatformObject")
case object PlatformEventNature extends SObjectNature("PlatformEvent")

final case class SObjectDetails(sobjectNature: SObjectNature,
                                typeName: TypeName,
                                fields: Seq[CustomFieldDeclaration],
                                fieldSets: Set[Name],
                                sharingReasons: Set[Name]) {

  def isIntroducing(module: Module): Boolean = {
    if (sobjectNature.isInstanceOf[IntroducingNature]) {
      EncodedName(typeName.name).namespace == module.namespace
    } else {
      sobjectNature == CustomMetadataNature || sobjectNature == BigObjectNature || sobjectNature == PlatformEventNature
    }
  }

  def withTypeName(newTypeName: TypeName): SObjectDetails = {
    SObjectDetails(sobjectNature, newTypeName, fields, fieldSets, sharingReasons)
  }
}

object SObjectDetails {
  def parseSObject(path: PathLike, module: Module): Option[SObjectDetails] = {
    val dt = MetadataDocument(path)
    assert(dt.exists(_.isInstanceOf[SObjectLike]))
    val typeName = TypeName(EncodedName(dt.get.name).defaultNamespace(module.namespace).fullName,
                            Nil,
                            Some(TypeNames.Schema))

    // TODO: Improve handling of ghosted SObject types
    if (!path.exists) {
      val sobjectNature: SObjectNature = dt match {
        case Some(x: SObjectDocument) if x.name.value.endsWith("__c") => CustomObjectNature
        case Some(_: SObjectDocument)                                 => PlatformObjectNature
      }

      val sfdxFields = parseSfdxFields(path, module, typeName, sobjectNature)
      val sfdxFieldSets = parseSfdxFieldSets(path, module)
      val sfdxSharingReasons = parseSfdxSharingReason(path, module)
      return Some(
        SObjectDetails(sobjectNature,
                       typeName,
                       sfdxFields,
                       sfdxFieldSets.toSet,
                       sfdxSharingReasons.toSet))
    }

    val parseResult = XMLFactory.parse(path)
    if (parseResult.isLeft) {
      OrgImpl.log(parseResult.swap.getOrElse(throw new NoSuchElementException))
      return None
    }
    val rootElement = parseResult.getOrElse(throw new NoSuchElementException).rootElement

    try {
      rootElement.checkIsOrThrow("CustomObject")

      val sobjectNature: SObjectNature = dt match {
        case Some(_: CustomMetadataDocument) => CustomMetadataNature
        case Some(_: BigObjectDocument)      => BigObjectNature
        case Some(_: PlatformEventDocument)  => PlatformEventNature
        case Some(x: SObjectDocument) if x.name.value.endsWith("__c") =>
          rootElement.getOptionalSingleChildAsString("customSettingsType") match {
            case Some("List")      => ListCustomSettingNature
            case Some("Hierarchy") => HierarchyCustomSettingsNature
            case Some(x) =>
              OrgImpl.logError(
                PathLocation(path.toString, Location(rootElement.line)),
                s"Unexpected customSettingsType value '$x', should be 'List' or 'Hierarchy'")
              CustomObjectNature
            case _ => CustomObjectNature
          }
        case Some(_: SObjectDocument) => PlatformObjectNature
      }

      val fields = rootElement
        .getChildren("fields")
        .flatMap(f => CustomFieldDeclaration.parseField(f, path, module, typeName, sobjectNature))
      val sfdxFields = parseSfdxFields(path, module, typeName, sobjectNature)

      val fieldSets = rootElement
        .getChildren("fieldSets")
        .map(f => parseFieldSet(f, path, module))
      val sfdxFieldSets = parseSfdxFieldSets(path, module)

      val sharingReasons = rootElement
        .getChildren("sharingReasons")
        .map(f => parseSharingReason(f, path, module))
      val sfdxSharingReasons = parseSfdxSharingReason(path, module)

      Some(
        SObjectDetails(sobjectNature,
                       typeName,
                       fields ++ sfdxFields,
                       (fieldSets ++ sfdxFieldSets).toSet,
                       (sharingReasons ++ sfdxSharingReasons).toSet))

    } catch {
      case e: XMLException =>
        OrgImpl.logError(PathLocation(path.toString, e.where), e.msg)
        None
    }
  }

  private def parseFieldSet(elem: XMLElementLike, path: PathLike, module: Module): Name = {
    EncodedName(elem.getSingleChildAsString("fullName"))
      .defaultNamespace(module.namespace)
      .fullName
  }

  private def parseSharingReason(elem: XMLElementLike, path: PathLike, module: Module): Name = {
    EncodedName(elem.getSingleChildAsString("fullName"))
      .defaultNamespace(module.namespace)
      .fullName
  }

  private def parseSfdxFields(path: PathLike,
                              module: Module,
                              sObjectType: TypeName,
                              sObjectNature: SObjectNature): Seq[CustomFieldDeclaration] = {

    val fieldsDir = path.parent.join("fields")
    if (!fieldsDir.isDirectory)
      return Seq()

    fieldsDir.directoryList() match {
      case Left(_) => Seq()
      case Right(entries) =>
        entries
          .filter(_.endsWith(".field-meta.xml"))
          .flatMap(entry => {
            val fieldPath = fieldsDir.join(entry)
            try {
              val parseResult = XMLFactory.parse(fieldPath)
              if (parseResult.isLeft) {
                OrgImpl.log(parseResult.swap.getOrElse(throw new NoSuchElementException))
                None
              } else {
                val rootElement =
                  parseResult.getOrElse(throw new NoSuchElementException).rootElement
                rootElement.checkIsOrThrow("CustomField")
                CustomFieldDeclaration
                  .parseField(rootElement, fieldPath, module, sObjectType, sObjectNature)
              }
            } catch {
              case e: XMLException =>
                OrgImpl.logError(PathLocation(fieldPath.toString, e.where), e.msg)
                None
            }
          })
    }
  }

  private def parseSfdxFieldSets(path: PathLike, module: Module): Seq[Name] = {
    val fieldSetDir = path.parent.join("fieldSets")
    if (!fieldSetDir.isDirectory)
      return Seq()

    fieldSetDir.directoryList() match {
      case Left(_) => Seq()
      case Right(entries) =>
        entries
          .filter(_.endsWith(".fieldSet-meta.xml"))
          .flatMap(entry => {
            val fieldSetsPaths = fieldSetDir.join(entry)
            try {
              val parseResult = XMLFactory.parse(fieldSetsPaths)
              if (parseResult.isLeft) {
                OrgImpl.log(parseResult.swap.getOrElse(throw new NoSuchElementException))
                None
              } else {
                val rootElement =
                  parseResult.getOrElse(throw new NoSuchElementException).rootElement
                rootElement.checkIsOrThrow("FieldSet")
                Some(parseFieldSet(rootElement, fieldSetsPaths, module))
              }
            } catch {
              case e: XMLException =>
                OrgImpl.logError(PathLocation(fieldSetsPaths.toString, e.where), e.msg)
                None
            }
          })
    }
  }

  private def parseSfdxSharingReason(path: PathLike, module: Module): Seq[Name] = {
    val dir = path.parent.join("sharingReasons")
    if (!dir.isDirectory)
      return Seq()

    dir.directoryList() match {
      case Left(_) => Seq()
      case Right(entries) =>
        entries
          .filter(_.endsWith(".sharingReason-meta.xml"))
          .flatMap(entry => {
            val path = dir.join(entry)
            try {
              val parseResult = XMLFactory.parse(path)
              if (parseResult.isLeft) {
                OrgImpl.log(parseResult.swap.getOrElse(throw new NoSuchElementException))
                None
              } else {
                val rootElement =
                  parseResult.getOrElse(throw new NoSuchElementException).rootElement
                rootElement.checkIsOrThrow("SharingReason")
                Some(parseSharingReason(rootElement, path, module))
              }
            } catch {
              case e: XMLException =>
                OrgImpl.logError(PathLocation(path.toString, e.where), e.msg)
                None
            }
          })
    }
  }

}