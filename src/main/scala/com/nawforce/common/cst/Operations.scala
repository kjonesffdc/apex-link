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
 3. the name of the author may not be used to endorse or promote products
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
package com.nawforce.common.cst

import com.nawforce.common.names.TypeName
import com.nawforce.common.types.TypeDeclaration
import com.nawforce.common.types.platform.PlatformTypes

trait AssignableSupport {
  def isAssignable(toType: TypeName, fromType: TypeDeclaration, context: VerifyContext): Boolean = {
    if (fromType.typeName == TypeName.Null ||
      (fromType.typeName == toType) ||
      (toType == TypeName.InternalObject) ||
      context.pkg.isGhostedType(toType)) {
      true
    } else if (fromType.typeName.isRecordSet) {
      isRecordSetAssignable(toType, context)
    } else if (toType.params.nonEmpty || fromType.typeName.params.nonEmpty) {
      isAssignableGeneric(toType, fromType, context)
    } else {
      AssignableSupport.baseAssignable.contains(toType, fromType.typeName) ||
        fromType.extendsOrImplements(toType)
    }
  }

  def isAssignable(toType: TypeName, fromType: TypeName, context: VerifyContext): Boolean = {
    context.getTypeFor(fromType, context.thisType) match {
      case Left(_) => false
      case Right(fromDeclaration) => isAssignable(toType, fromDeclaration, context)
    }
  }

  private def isAssignableGeneric(toType: TypeName, fromType: TypeDeclaration, context: VerifyContext): Boolean = {
    if (toType == fromType.typeName) {
      true
    } else if (toType.params.size == fromType.typeName.params.size) {
      isSObjectListAssignment(toType, fromType, context) || {
        // Future: This is over general, not supported on Set & Map, doh
        val sameParams = toType.withParams(fromType.typeName.params)
        (fromType.typeName == sameParams || fromType.extendsOrImplements(sameParams)) &&
          toType.params.zip(fromType.typeName.params).map(p => isAssignable(p._1, p._2, context)).forall(b =>b)
      }
    } else if (toType.params.isEmpty || fromType.typeName.params.isEmpty) {
      fromType.extendsOrImplements(toType)
    } else {
      false
    }
  }

  private def isSObjectListAssignment(toType: TypeName, fromType: TypeDeclaration, context: VerifyContext): Boolean = {
    if (toType.isList && fromType.typeName.isList &&
      fromType.typeName.params.head == TypeName.SObject &&
      toType.params.head != TypeName.SObject) {
      context.getTypeFor(toType.params.head, context.thisType) match {
        case Left(_) => false
        case Right(toDeclaration) => toDeclaration.isSObject
      }
    } else {
      false
    }
  }

  @scala.annotation.tailrec
  private def isRecordSetAssignable(toType: TypeName, context: VerifyContext): Boolean = {
    if (toType == TypeName.SObject || toType.isSObjectList || toType.isObjectList) {
      true
    } else if (toType.isList) {
      isRecordSetAssignable(toType.params.head, context)
    } else {
      context.getTypeFor(toType, context.thisType) match {
        case Left(_) => false
        case Right(toDeclaration) => toDeclaration.isSObject
      }
    }
  }


  def couldBeEqual(toType: TypeDeclaration, fromType: TypeDeclaration, context: VerifyContext): Boolean = {
    isAssignable(toType.typeName, fromType, context) || isAssignable(fromType.typeName, toType, context)
  }
}

object AssignableSupport {
  private lazy val baseAssignable: Set[(TypeName, TypeName)] = Set(
    (TypeName.Long, TypeName.Integer),
    (TypeName.Decimal, TypeName.Integer),
    (TypeName.Double, TypeName.Integer),
    (TypeName.Decimal, TypeName.Long),
    (TypeName.Double, TypeName.Long),
    (TypeName.Double, TypeName.Decimal),
    (TypeName.Decimal, TypeName.Double),
    (TypeName.Id, TypeName.String),
    (TypeName.String, TypeName.Id),
    (TypeName.Datetime, TypeName.Date)
  )
}

abstract class Operation extends AssignableSupport {
  def verify(leftType: ExprContext, rightContext: ExprContext, op: String,
             context: VerifyContext): Either[String, ExprContext]

  def getCommonBase(toType: TypeDeclaration, fromType: TypeDeclaration, context: VerifyContext): Option[TypeDeclaration] = {
    val toSupertypes = toType.superTypes()
    val fromSupertypes = fromType.superTypes()
    val common = toSupertypes.intersect(fromSupertypes)
    common.headOption.flatMap(context.getTypeFor(_, context.thisType).toOption)
  }

  def isNumericKind(typeName: TypeName): Boolean = {
    typeName == TypeName.Integer ||
    typeName == TypeName.Long ||
    typeName == TypeName.Decimal ||
    typeName == TypeName.Double
  }

  def isStringKind(typeName: TypeName): Boolean = {
    typeName == TypeName.String ||
    typeName == TypeName.Id
  }

  def isDateKind(typeName: TypeName): Boolean = {
    typeName == TypeName.Date ||
    typeName == TypeName.Datetime
  }

  def isNonReferenceKind(typeName: TypeName): Boolean = {
    Operation.nonReferenceTypes.contains(typeName)
  }

  def getArithmeticResult(leftType: TypeName, rightType: TypeName): Option[TypeDeclaration] = {
    Operation.arithmeticOps.get((leftType, rightType))
  }

  def getArithmeticAddSubtractAssigmentResult(leftType: TypeName, rightType: TypeName): Option[TypeDeclaration] = {
    Operation.arithmeticAddSubtractAssigmentOps.get((leftType, rightType))
  }

  def getArithmeticMultiplyDivideAssigmentResult(leftType: TypeName, rightType: TypeName): Option[TypeDeclaration] = {
    Operation.arithmeticMultiplyDivideAssigmentOps.get((leftType, rightType))
  }

  def getBitwiseResult(leftType: TypeName, rightType: TypeName): Option[TypeDeclaration] = {
    Operation.bitwiseOps.get((leftType, rightType))
  }

  def getBitwiseAssignmentResult(leftType: TypeName, rightType: TypeName): Option[TypeDeclaration] = {
    Operation.bitwiseAssignmentOps.get((leftType, rightType))
  }
}

object Operation {
  private lazy val nonReferenceTypes: Set[TypeName] = Set (
    TypeName.Integer,
    TypeName.Long,
    TypeName.Decimal,
    TypeName.Double,
    TypeName.String,
    TypeName.Date,
    TypeName.Datetime,
    TypeName.Time,
    TypeName.Blob,
    TypeName.Id
  )

  private lazy val arithmeticOps: Map[(TypeName, TypeName), TypeDeclaration] = Map(
    (TypeName.Integer, TypeName.Integer) -> PlatformTypes.integerType,
    (TypeName.Integer, TypeName.Long) -> PlatformTypes.longType,
    (TypeName.Integer, TypeName.Decimal) -> PlatformTypes.decimalType,
    (TypeName.Integer, TypeName.Double) -> PlatformTypes.doubleType,
    (TypeName.Long, TypeName.Integer) -> PlatformTypes.longType,
    (TypeName.Long, TypeName.Long) -> PlatformTypes.longType,
    (TypeName.Long, TypeName.Decimal) -> PlatformTypes.decimalType,
    (TypeName.Long, TypeName.Double) -> PlatformTypes.doubleType,
    (TypeName.Decimal, TypeName.Integer) -> PlatformTypes.decimalType,
    (TypeName.Decimal, TypeName.Long) -> PlatformTypes.decimalType,
    (TypeName.Decimal, TypeName.Decimal) -> PlatformTypes.decimalType,
    (TypeName.Decimal, TypeName.Double) -> PlatformTypes.decimalType,
    (TypeName.Double, TypeName.Integer) -> PlatformTypes.doubleType,
    (TypeName.Double, TypeName.Long) -> PlatformTypes.doubleType,
    (TypeName.Double, TypeName.Decimal) -> PlatformTypes.decimalType,
    (TypeName.Double, TypeName.Double) -> PlatformTypes.doubleType,
    (TypeName.Date, TypeName.Integer) -> PlatformTypes.dateType,
    (TypeName.Date, TypeName.Long) -> PlatformTypes.dateType,
    (TypeName.Datetime, TypeName.Integer) -> PlatformTypes.datetimeType,
    (TypeName.Datetime, TypeName.Long) -> PlatformTypes.datetimeType,
    (TypeName.Datetime, TypeName.Decimal) -> PlatformTypes.datetimeType,
    (TypeName.Datetime, TypeName.Double) -> PlatformTypes.datetimeType,
    (TypeName.Time, TypeName.Integer) -> PlatformTypes.timeType,
    (TypeName.Time, TypeName.Long) -> PlatformTypes.timeType,
  )

  private lazy val arithmeticAddSubtractAssigmentOps: Map[(TypeName, TypeName), TypeDeclaration] = Map(
    (TypeName.Integer, TypeName.Integer) -> PlatformTypes.integerType,
    (TypeName.Long, TypeName.Integer) -> PlatformTypes.longType,
    (TypeName.Long, TypeName.Long) -> PlatformTypes.longType,
    (TypeName.Decimal, TypeName.Integer) -> PlatformTypes.decimalType,
    (TypeName.Decimal, TypeName.Long) -> PlatformTypes.decimalType,
    (TypeName.Decimal, TypeName.Double) -> PlatformTypes.decimalType,
    (TypeName.Decimal, TypeName.Decimal) -> PlatformTypes.decimalType,
    (TypeName.Double, TypeName.Integer) -> PlatformTypes.doubleType,
    (TypeName.Double, TypeName.Long) -> PlatformTypes.doubleType,
    (TypeName.Double, TypeName.Decimal) -> PlatformTypes.decimalType,
    (TypeName.Double, TypeName.Double) -> PlatformTypes.doubleType,
    (TypeName.Date, TypeName.Integer) -> PlatformTypes.dateType,
    (TypeName.Date, TypeName.Long) -> PlatformTypes.dateType,
    (TypeName.Datetime, TypeName.Integer) -> PlatformTypes.datetimeType,
    (TypeName.Datetime, TypeName.Long) -> PlatformTypes.datetimeType,
    (TypeName.Time, TypeName.Integer) -> PlatformTypes.timeType,
    (TypeName.Time, TypeName.Long) -> PlatformTypes.timeType,
  )

  private lazy val arithmeticMultiplyDivideAssigmentOps: Map[(TypeName, TypeName), TypeDeclaration] = Map(
    (TypeName.Integer, TypeName.Integer) -> PlatformTypes.integerType,
    (TypeName.Long, TypeName.Integer) -> PlatformTypes.longType,
    (TypeName.Long, TypeName.Long) -> PlatformTypes.longType,
    (TypeName.Decimal, TypeName.Integer) -> PlatformTypes.decimalType,
    (TypeName.Decimal, TypeName.Long) -> PlatformTypes.decimalType,
    (TypeName.Decimal, TypeName.Double) -> PlatformTypes.decimalType,
    (TypeName.Decimal, TypeName.Decimal) -> PlatformTypes.decimalType,
    (TypeName.Double, TypeName.Integer) -> PlatformTypes.doubleType,
    (TypeName.Double, TypeName.Long) -> PlatformTypes.doubleType,
    (TypeName.Double, TypeName.Decimal) -> PlatformTypes.decimalType,
    (TypeName.Double, TypeName.Double) -> PlatformTypes.doubleType,
  )

  private lazy val bitwiseOps: Map[(TypeName, TypeName), TypeDeclaration] = Map(
    (TypeName.Integer, TypeName.Integer) -> PlatformTypes.integerType,
    (TypeName.Integer, TypeName.Long) -> PlatformTypes.longType,
    (TypeName.Long, TypeName.Integer) -> PlatformTypes.longType,
    (TypeName.Long, TypeName.Long) -> PlatformTypes.longType,
    (TypeName.Boolean, TypeName.Boolean) -> PlatformTypes.booleanType,
  )

  private lazy val bitwiseAssignmentOps: Map[(TypeName, TypeName), TypeDeclaration] = Map(
    (TypeName.Integer, TypeName.Integer) -> PlatformTypes.integerType,
    (TypeName.Long, TypeName.Integer) -> PlatformTypes.longType,
    (TypeName.Long, TypeName.Long) -> PlatformTypes.longType,
    (TypeName.Boolean, TypeName.Boolean) -> PlatformTypes.longType,
  )
}

case object AssignmentOperation extends Operation {
  override def verify(leftContext: ExprContext, rightContext: ExprContext,
                      op: String, context: VerifyContext): Either[String, ExprContext] = {
    if (rightContext.typeName == TypeName.Null) {
      Right(leftContext)
    } else if (isAssignable(leftContext.typeName, rightContext.typeDeclaration, context)) {
      Right(leftContext)
    } else {
      Left(s"Incompatible types in assignment, from '${rightContext.typeName}' to '${leftContext.typeName}'")
    }
  }
}

case object LogicalOperation extends Operation {
  override def verify(leftContext: ExprContext, rightContext: ExprContext,
                      op: String, context: VerifyContext): Either[String, ExprContext] = {
    if (!isAssignable(TypeName.Boolean, rightContext.typeDeclaration, context)) {
      Left(s"Right expression of logical $op must a boolean, not '${rightContext.typeName}'")
    } else if (!isAssignable(TypeName.Boolean, leftContext.typeDeclaration, context)) {
      Left(s"Left expression of logical $op must a boolean, not '${leftContext.typeName}'")
    } else {
      Right(leftContext)
    }
  }
}

case object CompareOperation extends Operation {
  override def verify(leftContext: ExprContext, rightContext: ExprContext,
                      op: String, context: VerifyContext): Either[String, ExprContext] = {

    if (isNumericKind(leftContext.typeName)) {
      if (!isNumericKind(rightContext.typeName)) {
        return Left(s"Comparing incompatible types '${leftContext.typeName}' and '${rightContext.typeName}'")
      }
    } else if (isStringKind(leftContext.typeName)) {
      if (!isStringKind(rightContext.typeName))
        return Left(s"Comparing incompatible types '${leftContext.typeName}' and '${rightContext.typeName}'")
    } else if (isDateKind(leftContext.typeName)) {
      if (!isDateKind(rightContext.typeName)) {
        return Left(s"Comparing incompatible types '${leftContext.typeName}' and '${rightContext.typeName}'")
      }
    } else {
      return Left(s"Comparing incompatible types '${leftContext.typeName}' and '${rightContext.typeName}'")
    }
    Right(ExprContext(isStatic = Some(false), PlatformTypes.booleanType))
  }
}

case object ExactEqualityOperation extends Operation {
  override def verify(leftContext: ExprContext, rightContext: ExprContext,
                      op: String, context: VerifyContext): Either[String, ExprContext] = {

    if (isNonReferenceKind(leftContext.typeName)) {
      Left(s"Exact equality/inequality requires is not supported on non-reference types '${leftContext.typeName}'")
    } else if (isNonReferenceKind(rightContext.typeName)) {
      Left(s"Exact equality/inequality requires is not supported on non-reference types '${rightContext.typeName}'")
    } else if (
      leftContext.typeName == rightContext.typeName ||
      leftContext.typeName == TypeName.InternalObject ||
      rightContext.typeName == TypeName.InternalObject ||
      leftContext.typeDeclaration.extendsOrImplements(rightContext.typeName) ||
      rightContext.typeDeclaration.extendsOrImplements(leftContext.typeName)
    )
      Right(ExprContext(isStatic = Some(false), PlatformTypes.booleanType))
    else
      Left(s"Comparing incompatible types '${leftContext.typeName}' and '${rightContext.typeName}'")
  }
}

case object EqualityOperation extends Operation {
  override def verify(leftContext: ExprContext, rightContext: ExprContext,
                      op: String, context: VerifyContext): Either[String, ExprContext] = {
    if (couldBeEqual(leftContext.typeDeclaration, rightContext.typeDeclaration, context))
      Right(ExprContext(isStatic = Some(false), PlatformTypes.booleanType))
    else
      Left(s"Comparing incompatible types '${leftContext.typeName}' and '${rightContext.typeName}'")
  }
}

case object PlusOperation extends Operation {
  override def verify(leftContext: ExprContext, rightContext: ExprContext,
                      op: String, context: VerifyContext): Either[String, ExprContext] = {
    if (leftContext.typeName == TypeName.String || rightContext.typeName == TypeName.String) {
      Right(ExprContext(isStatic = Some(false), PlatformTypes.stringType))
    }
    else {
      val td = getArithmeticResult(leftContext.typeName, rightContext.typeName)
      if (td.isEmpty) {
        return Left(s"Addition operation not allowed between types '${leftContext.typeName}' and '${rightContext.typeName}'")
      }
      Right(ExprContext(isStatic = Some(false), td.get))
    }
  }
}

case object ArithmeticOperation extends Operation {
  override def verify(leftContext: ExprContext, rightContext: ExprContext,
                      op: String, context: VerifyContext): Either[String, ExprContext] = {
    val td = getArithmeticResult(leftContext.typeName, rightContext.typeName)
    if (td.isEmpty) {
      return Left(s"Arithmetic operation not allowed between types '${leftContext.typeName}' and '${rightContext.typeName}'")
    }
    Right(ExprContext(isStatic = Some(false), td.get))
  }
}

case object ArithmeticAddSubtractAssignmentOperation extends Operation {
  override def verify(leftContext: ExprContext, rightContext: ExprContext,
                      op: String, context: VerifyContext): Either[String, ExprContext] = {
    if (leftContext.typeName == TypeName.String && op == "+=") {
      Right(ExprContext(isStatic = Some(false), PlatformTypes.stringType))
    } else {
      val td = getArithmeticAddSubtractAssigmentResult(leftContext.typeName, rightContext.typeName)
      if (td.isEmpty) {
        return Left(s"Arithmetic operation not allowed between types '${leftContext.typeName}' and '${rightContext.typeName}'")
      }
      Right(ExprContext(isStatic = Some(false), td.get))
    }
  }
}

case object ArithmeticMultiplyDivideAssignmentOperation extends Operation {
  override def verify(leftContext: ExprContext, rightContext: ExprContext,
                      op: String, context: VerifyContext): Either[String, ExprContext] = {
    val td = getArithmeticMultiplyDivideAssigmentResult(leftContext.typeName, rightContext.typeName)
    if (td.isEmpty) {
      return Left(s"Arithmetic operation not allowed between types '${leftContext.typeName}' and '${rightContext.typeName}'")
    }
    Right(ExprContext(isStatic = Some(false), td.get))
  }
}

case object BitwiseOperation extends Operation {
  override def verify(leftContext: ExprContext, rightContext: ExprContext,
                      op: String, context: VerifyContext): Either[String, ExprContext] = {
    val td = getBitwiseResult(leftContext.typeName, rightContext.typeName)
    if (td.isEmpty) {
      return Left(s"Bitwise operation only allowed between Integer, Long & Boolean types, not '${leftContext.typeName}' and '${rightContext.typeName}'")
    }
    Right(ExprContext(isStatic = Some(false), td.get))
  }
}

case object BitwiseAssignmentOperation extends Operation {
  override def verify(leftContext: ExprContext, rightContext: ExprContext,
                      op: String, context: VerifyContext): Either[String, ExprContext] = {
    val td = getBitwiseAssignmentResult(leftContext.typeName, rightContext.typeName)
    if (td.isEmpty) {
      return Left(s"Bitwise operation only allowed between Integer, Long & Boolean types, not '${leftContext.typeName}' and '${rightContext.typeName}'")
    }
    Right(ExprContext(isStatic = Some(false), td.get))
  }
}

case object ConditionalOperation extends Operation {
  override def verify(leftContext: ExprContext, rightContext: ExprContext,
                      op: String, context: VerifyContext): Either[String, ExprContext] = {

    // Future: How does this really function, Java mechanics are very complex
    if (isAssignable(leftContext.typeName, rightContext.typeDeclaration, context)) {
      Right(leftContext)
    } else if (isAssignable(rightContext.typeName, leftContext.typeDeclaration, context)) {
      Right(rightContext)
    } else {
      getCommonBase(leftContext.typeDeclaration, rightContext.typeDeclaration, context)
          .map(td => Right(ExprContext(isStatic = Some(false), td)))
          .getOrElse({
            Left(s"Incompatible types in ternary operation '${leftContext.typeName}' and '${rightContext.typeName}'")
          })
    }
  }
}
