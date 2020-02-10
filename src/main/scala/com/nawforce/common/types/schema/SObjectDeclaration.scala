/*
 [The "BSD licence"]
 Copyright (c) 2019 Kevin Jones
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
package com.nawforce.common.types.schema

import com.nawforce.common.api.Org
import com.nawforce.common.cst.VerifyContext
import com.nawforce.common.documents._
import com.nawforce.common.finding.TypeRequest
import com.nawforce.common.names.{DotName, Name, TypeName}
import com.nawforce.common.path.PathLike
import com.nawforce.common.types._
import com.nawforce.common.types.pkg.PackageDeclaration
import com.nawforce.common.types.platform.PlatformTypes

import scala.collection.mutable

final case class SObjectDeclaration(pkg: PackageDeclaration, _typeName: TypeName,
                                    sobjectNature: SObjectNature, fieldSets: Set[Name],
                                    override val fields: Seq[FieldDeclaration], override val isComplete: Boolean)
  extends NamedTypeDeclaration(pkg, _typeName) {

  override val superClass: Option[TypeName] = Some(TypeName.SObject)
  override lazy val superClassDeclaration: Option[TypeDeclaration] = {
    pkg.getTypeFor(TypeName.SObject, this)
  }

  override def findField(name: Name, staticContext: Option[Boolean]): Option[FieldDeclaration] = {
    super.findFieldSObject(name, staticContext).orElse({
      val field = pkg.schema().relatedLists.findField(typeName, name)
      if (field.nonEmpty && staticContext.contains(true)) {
        Some(CustomFieldDeclaration(field.get.name, TypeName.sObjectFields$(typeName), asStatic = true))
      } else {
        field
      }
    })
  }

  override def findMethod(name: Name, params: Seq[TypeName], staticContext: Option[Boolean],
                          verifyContext: VerifyContext): Seq[MethodDeclaration] = {
    if (staticContext.contains(true)) {
      val customMethods = sobjectNature match {
        case HierarchyCustomSettingsNature => hierarchyCustomSettingsMethods
        case ListCustomSettingNature => listCustomSettingsMethods
        case _ => SObjectDeclaration.sObjectMethodMap
      }
      val customMethod = customMethods.get((name, params.size))
      if (customMethod.nonEmpty)
        return customMethod.toSeq
    }
    defaultFindMethod(name, params, staticContext, verifyContext)
  }

  def defaultFindMethod(name: Name, params: Seq[TypeName], staticContext: Option[Boolean],
                        verifyContext: VerifyContext): Seq[MethodDeclaration] = {
    val clone = cloneMethods.get((name, params.size, staticContext.contains(true)))
    if (clone.nonEmpty)
      clone.toSeq
    else
      PlatformTypes.sObjectType.findMethod(name, params, staticContext, verifyContext)
  }

  private lazy val cloneMethods: Map[(Name, Int, Boolean), MethodDeclaration] = {
    val preserveId = CustomParameterDeclaration(Name("preserveId"), TypeName.Boolean)
    val isDeepClone = CustomParameterDeclaration(Name("isDeepClone"), TypeName.Boolean)
    val preserveReadOnlyTimestamps = CustomParameterDeclaration(Name("preserveReadOnlyTimestamps"), TypeName.Boolean)
    val preserveAutonumber = CustomParameterDeclaration(Name("preserveAutonumber"), TypeName.Boolean)
    Seq(
      CustomMethodDeclaration(Name("clone"), typeName, Seq()),
      CustomMethodDeclaration(Name("clone"), typeName, Seq(preserveId)),
      CustomMethodDeclaration(Name("clone"), typeName, Seq(preserveId, isDeepClone)),
      CustomMethodDeclaration(Name("clone"), typeName, Seq(preserveId, isDeepClone, preserveReadOnlyTimestamps)),
      CustomMethodDeclaration(Name("clone"), typeName, Seq(preserveId, isDeepClone, preserveReadOnlyTimestamps, preserveAutonumber))
    ).map(m => ((m.name, m.parameters.size, m.isStatic),m)).toMap
  }

  private lazy val hierarchyCustomSettingsMethods: Map[(Name, Int), MethodDeclaration] =
    Seq(
      CustomMethodDeclaration(Name("getInstance"), typeName, Seq()),
      CustomMethodDeclaration(Name("getInstance"), typeName, Seq(CustomParameterDeclaration(Name("Id"), TypeName.Id))),
      CustomMethodDeclaration(Name("getOrgDefaults"), typeName, Seq()),
      CustomMethodDeclaration(Name("getValues"), typeName, Seq(CustomParameterDeclaration(Name("Id"), TypeName.Id))),
    ).map(m => ((m.name, m.parameters.size),m)).toMap

  private lazy val listCustomSettingsMethods: Map[(Name, Int), MethodDeclaration] =
    Seq(
      CustomMethodDeclaration(Name("getAll"), TypeName.mapOf(TypeName.String, typeName), Seq()),
      CustomMethodDeclaration(Name("getInstance"), typeName, Seq()),
      CustomMethodDeclaration(Name("getInstance"), typeName, Seq(CustomParameterDeclaration(Name("Name"), TypeName.String))),
      CustomMethodDeclaration(Name("getValues"), typeName, Seq(CustomParameterDeclaration(Name("Name"), TypeName.String))),
    ).map(m => ((m.name, m.parameters.size),m)).toMap
}

object SObjectDeclaration {
  private lazy val sObjectMethodMap: Map[(Name, Int), MethodDeclaration] =
    PlatformTypes.sObjectType.methods.map(m => ((m.name, m.parameters.size),m)).toMap

  def create(pkg: PackageDeclaration, path: PathLike): Seq[TypeDeclaration] = {
    val sobjectDetailsOpt = SObjectDetails.parseSObject(path, pkg)
    if (sobjectDetailsOpt.isEmpty)
      return Seq()
    val sobjectDetails = sobjectDetailsOpt.get

    if (sobjectDetails.isIntroducing(pkg)) {
      createNew(sobjectDetails, pkg)
    } else {
        if (pkg.isGhostedType(sobjectDetails.typeName))
          Seq(extendExisting(sobjectDetails, pkg, None))
        else {
          createExisting(sobjectDetails, path, pkg)
        }
    }
  }

  private def createExisting(sobjectDetails: SObjectDetails, path: PathLike, pkg: PackageDeclaration) : Seq[TypeDeclaration] = {
    val typeName = sobjectDetails.typeName

    if (typeName.name == Name.Activity) {
      // Fake Activity as applying to Task & Event, how bizarre is that
      createExisting(sobjectDetails.withTypeName(typeName.withName(Name.Task)), path, pkg) ++
        createExisting(sobjectDetails.withTypeName(typeName.withName(Name.Event)), path, pkg)
    } else {
      val sobjectType = TypeRequest(typeName, pkg, excludeSObjects = false).toOption
      if (sobjectType.isEmpty || !sobjectType.get.superClassDeclaration.exists(superClass => superClass.typeName == TypeName.SObject)) {
        Org.logMessage(LineLocation(path, 0), s"No SObject declaration found for '$typeName'")
        return Seq()
      }
      Seq(extendExisting(sobjectDetails, pkg, sobjectType))
    }
  }

  private def createNew(sobjectDetails: SObjectDetails, pkg: PackageDeclaration): Seq[TypeDeclaration] = {
    val typeName = sobjectDetails.typeName

    val fields =
      if (sobjectDetails.sobjectNature == CustomMetadataNature)
        customMetadataFields(sobjectDetails)
      else if (sobjectDetails.sobjectNature == PlatformEventNature)
        platformEventFields(sobjectDetails)
      else
        customObjectFields(sobjectDetails)

    val supportObjects: Seq[SObjectDeclaration] =
      if (sobjectDetails.isIntroducing(pkg) &&
        sobjectDetails.sobjectNature != CustomMetadataNature && sobjectDetails.sobjectNature != PlatformEventNature) {
        Seq(
          // TODO: Check fields & when should be available
          createShare(pkg, typeName),
          createFeed(pkg, typeName),
          createHistory(pkg, typeName)
        )
      } else Seq()

    val allObjects =
      new SObjectDeclaration(pkg, typeName, sobjectDetails.sobjectNature, sobjectDetails.fieldSets, fields, isComplete = true) +:
      supportObjects

    allObjects.foreach(pkg.schema().sobjectTypes.add)
    allObjects
  }

  private lazy val standardCustomObjectFields: Seq[FieldDeclaration] = {
    CustomFieldDeclaration(Name.Id, TypeName.Id) +:
      CustomFieldDeclaration(Name.NameName, TypeName.String) +:
      CustomFieldDeclaration(Name.RecordTypeId, TypeName.Id) +:
      CustomFieldDeclaration(Name("CreatedBy"), TypeName.User) +:
      CustomFieldDeclaration(Name("CreatedById"), TypeName.Id) +:
      CustomFieldDeclaration(Name("CreatedDate"), TypeName.Datetime) +:
      CustomFieldDeclaration(Name("LastModifiedBy"), TypeName.User) +:
      CustomFieldDeclaration(Name("LastModifiedById"), TypeName.Id) +:
      CustomFieldDeclaration(Name("LastModifiedDate"), TypeName.Datetime) +:
      CustomFieldDeclaration(Name("IsDeleted"), TypeName.Boolean) +:
      CustomFieldDeclaration(Name("SystemModstamp"), TypeName.Datetime) +:
      PlatformTypes.sObjectType.fields.filterNot(f => f.name == Name.SObjectType)
  }

  private def customObjectFields(sobjectDetails: SObjectDetails): Seq[FieldDeclaration] = {
    Seq(
      CustomFieldDeclaration(Name.SObjectType, TypeName.sObjectType$(sobjectDetails.typeName), asStatic = true),
      CustomFieldDeclaration(Name.Fields, TypeName.sObjectFields$(sobjectDetails.typeName), asStatic = true),
    ) ++
      standardCustomObjectFields ++
      sobjectDetails.fields ++
      (if (sobjectDetails.sobjectNature == HierarchyCustomSettingsNature)
        Seq(CustomFieldDeclaration(Name.SetupOwnerId, PlatformTypes.idType.typeName))
      else
        Seq()
        )
  }

  private lazy val standardCustomMetadataFields: Seq[FieldDeclaration] = {
    Seq(
      CustomFieldDeclaration(Name.Id, TypeName.Id),
      CustomFieldDeclaration(Name.DeveloperName, TypeName.String),
      CustomFieldDeclaration(Name.IsProtected, TypeName.Boolean),
      CustomFieldDeclaration(Name.Label, TypeName.String),
      CustomFieldDeclaration(Name.Language, TypeName.String),
      CustomFieldDeclaration(Name.MasterLabel, TypeName.String),
      CustomFieldDeclaration(Name.NamespacePrefix, TypeName.String),
      CustomFieldDeclaration(Name.QualifiedAPIName, TypeName.String))
  }

  private def customMetadataFields(sobjectDetails: SObjectDetails): Seq[FieldDeclaration] = {
    standardCustomMetadataFields ++
      sobjectDetails.fields :+
      CustomFieldDeclaration(Name.SObjectType, TypeName.sObjectType$(sobjectDetails.typeName), asStatic = true)
  }

  private lazy val standardPlatformEventFields: Seq[FieldDeclaration] = {
    Seq(CustomFieldDeclaration(Name.ReplayId, TypeName.String))
  }

  private def platformEventFields(sobjectDetails: SObjectDetails): Seq[FieldDeclaration] = {
    standardPlatformEventFields ++
      sobjectDetails.fields :+
      CustomFieldDeclaration(Name.SObjectType, TypeName.sObjectType$(sobjectDetails.typeName), asStatic = true)
  }

  private def extendExisting(sobjectDetails: SObjectDetails, pkg: PackageDeclaration, base: Option[TypeDeclaration]): TypeDeclaration = {
    val typeName = sobjectDetails.typeName
    val isComplete = base.nonEmpty && pkg.basePackages.forall(!_.isGhosted)

    val fields = collectBaseFields(typeName.asDotName, pkg)
    base.getOrElse(PlatformTypes.sObjectType).fields.foreach(field => fields.put(field.name, field))
    sobjectDetails.fields.foreach(field => {fields.put(field.name, field)})

    // TODO: Collect base fieldsets ?

    val td = new SObjectDeclaration(pkg, typeName, sobjectDetails.sobjectNature, sobjectDetails.fieldSets,
      fields.values.toSeq, isComplete)
    pkg.schema().sobjectTypes.add(td)
    td
  }

  private def collectBaseFields(sObject: DotName, pkg: PackageDeclaration): mutable.Map[Name, FieldDeclaration] = {
    val collected: mutable.Map[Name, FieldDeclaration] = mutable.Map()
    pkg.basePackages.filterNot(_.isGhosted).foreach(basePkg => {
      val fields: Seq[FieldDeclaration] = TypeRequest(sObject.asTypeName(), basePkg, excludeSObjects = false).toOption.map {
        case baseTd: SObjectDeclaration => baseTd.fields
        case _ => Nil
      }.getOrElse(Seq())
      fields.foreach(field => {collected.put(field.name, field)})
    })
    collected
  }

  private def createShare(pkg: PackageDeclaration, typeName: TypeName): SObjectDeclaration = {
    val shareName = typeName.withNameReplace("__c$", "__Share")
    val sobjectDetails = SObjectDetails(CustomObjectNature, shareName, Seq(), Set())
    SObjectDeclaration(pkg, shareName, CustomObjectNature, Set(),
      customObjectFields(sobjectDetails) ++ shareFields, isComplete = true)
  }

  private lazy val shareFields = PlatformTypes.sObjectType.fields ++ Seq(
    CustomFieldDeclaration(Name.AccessLevel, PlatformTypes.stringType.typeName),
    CustomFieldDeclaration(Name.ParentId, PlatformTypes.idType.typeName),
    CustomFieldDeclaration(Name.RowCause, PlatformTypes.stringType.typeName),
    CustomFieldDeclaration(Name.UserOrGroupId, PlatformTypes.idType.typeName)
  )

  private def createFeed(pkg: PackageDeclaration, typeName: TypeName): SObjectDeclaration = {
    val feedName = typeName.withNameReplace("__c$", "__Feed")
    val sobjectDetails = SObjectDetails(CustomObjectNature, feedName, Seq(), Set())
    SObjectDeclaration(pkg, feedName, CustomObjectNature, Set(),
      customObjectFields(sobjectDetails) ++ feedFields, isComplete = true)
  }

  private lazy val feedFields = PlatformTypes.sObjectType.fields ++ Seq(
    CustomFieldDeclaration(Name.BestCommentId, PlatformTypes.idType.typeName),
    CustomFieldDeclaration(Name.Body, PlatformTypes.stringType.typeName),
    CustomFieldDeclaration(Name.CommentCount, PlatformTypes.decimalType.typeName),
    CustomFieldDeclaration(Name.ConnectionId, PlatformTypes.idType.typeName),
    CustomFieldDeclaration(Name.InsertedById, PlatformTypes.idType.typeName),
    CustomFieldDeclaration(Name.IsRichText, PlatformTypes.booleanType.typeName),
    CustomFieldDeclaration(Name.LikeCount, PlatformTypes.decimalType.typeName),
    CustomFieldDeclaration(Name.LinkUrl, PlatformTypes.stringType.typeName),
    CustomFieldDeclaration(Name.NetworkScope, PlatformTypes.stringType.typeName),
    CustomFieldDeclaration(Name.ParentId, PlatformTypes.idType.typeName),
    CustomFieldDeclaration(Name.RelatedRecordId, PlatformTypes.idType.typeName),
    CustomFieldDeclaration(Name.Title, PlatformTypes.stringType.typeName),
    CustomFieldDeclaration(Name.Type, PlatformTypes.stringType.typeName),
    CustomFieldDeclaration(Name.Visibility, PlatformTypes.stringType.typeName),
  )

  private def createHistory(pkg: PackageDeclaration, typeName: TypeName): SObjectDeclaration = {
    val historyName = typeName.withNameReplace("__c$", "__History")
    val sobjectDetails = SObjectDetails(CustomObjectNature, historyName, Seq(), Set())
    SObjectDeclaration(pkg, historyName, CustomObjectNature, Set(),
      customObjectFields(sobjectDetails) ++ historyFields, isComplete = true)
  }

  private lazy val historyFields = PlatformTypes.sObjectType.fields ++ Seq(
    CustomFieldDeclaration(Name.Field, PlatformTypes.stringType.typeName),
    CustomFieldDeclaration(Name.NewValue, PlatformTypes.objectType.typeName),
    CustomFieldDeclaration(Name.OldValue, PlatformTypes.objectType.typeName),
  )
}