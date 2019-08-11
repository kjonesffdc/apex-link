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
package com.nawforce.types

import java.io.StringReader
import java.nio.file.{Path, Paths}

import com.nawforce.cst._
import com.nawforce.parsers.{ApexLexer, ApexParser, CaseInsensitiveInputStream}
import org.antlr.v4.runtime.CommonTokenStream

class TypeContextTest(_thisType: TypeName = null, _superType: TypeName = null, identifierTypes: Map[String, TypeName] = null) extends TypeContext {
  def thisType: TypeName =
    if (_thisType == null)
      throw new CSTException()
    else
      _thisType

  def superType: TypeName =
    if (_superType == null)
      throw new CSTException()
    else
      _superType

  def getIdentifierType(id: String): TypeName =
    if (identifierTypes == null || identifierTypes.get(id).isEmpty)
      throw new CSTException()
    else
      identifierTypes(id)
}

object TypeTestHelper {

  private val defaultPath = Paths.get("Dummy.cls")

  def typeLiteral(data: String, typeCtx: TypeContext): TypeName = {
    val context = new ConstructContext()
    Literal.construct(parse(defaultPath, data).literal(), context).getType(typeCtx)
  }

  def compareLiteral(p: String, r: TypeName, typeCtx: TypeContext): Unit = {
    val t = typeLiteral(p, typeCtx)
    if (t == null)
      throw new CSTException

    if (t != r) {
      System.out.println("Type mismatch:")
      System.out.println("Expected: " + r)
      System.out.println("Got: " + t)
      assert(false)
    }
  }

  private def parse(path: Path, data: String) = {
    val listener = new ThrowingErrorListener
    val cis: CaseInsensitiveInputStream = new CaseInsensitiveInputStream(path, new StringReader(data))

    val lexer: ApexLexer = new ApexLexer(cis)
    lexer.removeErrorListeners()
    lexer.addErrorListener(listener)

    val tokens: CommonTokenStream = new CommonTokenStream(lexer)
    val parser: ApexParser = new ApexParser(tokens)
    parser.removeErrorListeners()
    parser.addErrorListener(listener)
    parser
  }
}
