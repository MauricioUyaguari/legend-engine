// Copyright 2020 Goldman Sachs
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.finos.legend.engine.language.pure.compiler.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.finos.legend.engine.language.pure.compiler.Compiler;
import org.finos.legend.engine.language.pure.compiler.toPureGraph.PureModel;
import org.finos.legend.engine.protocol.pure.v1.model.context.EngineErrorType;
import org.finos.legend.engine.protocol.pure.v1.model.context.PureModelContextData;
import org.finos.legend.engine.shared.core.ObjectMapperFactory;
import org.finos.legend.engine.shared.core.identity.Identity;
import org.finos.legend.engine.shared.core.operational.errorManagement.EngineException;
import org.junit.Assert;

import java.util.Objects;
import java.util.Scanner;

public class TestCompilationFromProtocol
{
    public abstract static class TestCompilationFromProtocolTestSuite
    {
        private final ObjectMapper objectMapper = ObjectMapperFactory.getNewStandardObjectMapperWithPureProtocolExtensionSupports();

        public void testWithProtocolPath(String protocolPath)
        {
            testWithProtocolPath(protocolPath, null);
        }

        public void testWithProtocolPath(String protocolPath, String expectedErrorMsg)
        {
            String jsonString = new Scanner(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream(protocolPath), "Can't find resource '" + protocolPath + "'"), "UTF-8").useDelimiter("\\A").next();
            testWithJson(jsonString, expectedErrorMsg);
        }

        public void testWithJson(String pureModelContextDataJsonStr, String expectedErrorMsg)
        {
            try
            {
                PureModelContextData pureModelContextData = objectMapper.readValue(pureModelContextDataJsonStr, PureModelContextData.class);
                PureModel pureModel = Compiler.compile(pureModelContextData, null, Identity.getAnonymousIdentity().getName());
                pureModelContextData.getElements().parallelStream().forEach(pureModel::getPackageableElement);
                if (expectedErrorMsg != null)
                {
                    Assert.fail("Expected compilation error with message: " + expectedErrorMsg + "; but no error occurred");
                }
            }
            catch (EngineException e)
            {
                // NOTE: for error of this type, usually no source information is attached, unless we pass it into the JSON
                Assert.assertEquals(expectedErrorMsg, EngineException.buildPrettyErrorMessage(e.getMessage(), e.getSourceInformation(), EngineErrorType.COMPILATION));
            }
            catch (Exception e)
            {
                throw new RuntimeException(e);
            }
        }

        public void testProtocolLoadingModelWithPackageOffset(String protocolPath, String expectedErrorMsg, String offset)
        {
            String jsonString = new Scanner(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream(protocolPath), "Can't find resource '" + protocolPath + "'"), "UTF-8").useDelimiter("\\A").next();
            try
            {
                PureModelContextData pureModelContextData = objectMapper.readValue(jsonString, PureModelContextData.class);
                Compiler.compile(pureModelContextData, null, Identity.getAnonymousIdentity().getName(), offset, null);
                if (expectedErrorMsg != null)
                {
                    Assert.fail("Expected compilation error with message: " + expectedErrorMsg + "; but no error occurred");
                }
            }
            catch (EngineException e)
            {
                // NOTE: for error of this type, usually no source information is attached, unless we pass it into the JSON
                Assert.assertEquals(expectedErrorMsg, EngineException.buildPrettyErrorMessage(e.getMessage(), e.getSourceInformation(), EngineErrorType.COMPILATION));
            }
            catch (Exception e)
            {
                throw new RuntimeException(e);
            }
        }
    }
}
