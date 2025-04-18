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

package org.finos.legend.engine.language.pure.compiler.toPureGraph.handlers.builder;

import org.eclipse.collections.api.RichIterable;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.tuple.Pair;
import org.eclipse.collections.impl.list.mutable.FastList;
import org.finos.legend.engine.language.pure.compiler.toPureGraph.ProcessingContext;
import org.finos.legend.engine.language.pure.compiler.toPureGraph.PureModel;
import org.finos.legend.engine.language.pure.compiler.toPureGraph.ValueSpecificationBuilder;
import org.finos.legend.engine.language.pure.compiler.toPureGraph.handlers.FunctionHandler;
import org.finos.legend.engine.protocol.pure.m3.SourceInformation;
import org.finos.legend.engine.protocol.pure.m3.valuespecification.Variable;
import org.finos.legend.engine.protocol.pure.m3.valuespecification.constant.datatype.primitive.CString;
import org.finos.legend.engine.protocol.pure.m3.valuespecification.constant.classInstance.ClassInstance;
import org.finos.legend.engine.protocol.pure.m3.valuespecification.Collection;
import org.finos.legend.engine.protocol.pure.m3.function.LambdaFunction;
import org.finos.legend.engine.protocol.pure.m3.valuespecification.constant.PackageableElementPtr;
import org.finos.legend.pure.m3.coreinstance.meta.pure.metamodel.type.FunctionType;
import org.finos.legend.pure.m3.coreinstance.meta.pure.metamodel.valuespecification.SimpleFunctionExpression;
import org.finos.legend.pure.m3.coreinstance.meta.pure.metamodel.valuespecification.ValueSpecification;
import org.finos.legend.pure.m3.coreinstance.meta.pure.metamodel.valuespecification.VariableExpression;
import org.finos.legend.pure.m3.navigation.M3Paths;
import org.finos.legend.pure.m3.navigation.type.Type;

import java.util.List;
import java.util.Optional;

public abstract class FunctionExpressionBuilder
{
    public abstract Pair<SimpleFunctionExpression, List<ValueSpecification>> buildFunctionExpression(List<org.finos.legend.engine.protocol.pure.m3.valuespecification.ValueSpecification> parameters, SourceInformation sourceInformation, ValueSpecificationBuilder valueSpecificationBuilder);

    public abstract String getFunctionName();

    public boolean test(org.finos.legend.pure.m3.coreinstance.meta.pure.metamodel.function.Function<?> func, List<org.finos.legend.engine.protocol.pure.m3.valuespecification.ValueSpecification> parameters, PureModel pureModel, ProcessingContext processingContext)
    {
        RichIterable<? extends VariableExpression> vars = ((FunctionType) func._classifierGenericType()._typeArguments().getFirst()._rawType())._parameters();

        if (func._functionName().equals("letFunction"))
        {
            vars = FastList.newListWith(vars.getFirst(), (VariableExpression) processingContext.getInferredVariable(((CString) parameters.get(0)).value));
        }

        if (vars.size() == parameters.size())
        {
            return vars.zip(parameters).injectInto(true, (a, b) -> a && comp(b.getOne(), b.getTwo(), pureModel, processingContext));
        }
        return false;
    }

    private boolean comp(VariableExpression vv, org.finos.legend.engine.protocol.pure.m3.valuespecification.ValueSpecification vs, PureModel pureModel, ProcessingContext processingContext)
    {
        boolean isSignatureFunction = vv._genericType()._rawType() != null && Type.subTypeOf(vv._genericType()._rawType(), pureModel.getType("meta::pure::metamodel::function::Function"), pureModel.getExecutionSupport().getProcessorSupport());
        boolean isParamFunction = vs instanceof LambdaFunction ||
                                  (vs instanceof ClassInstance && ((ClassInstance) vs).type.equals("path")) ||
                                  isPackageableElementSubtypeOfFunction(vs, processingContext, pureModel) ||
                                  isVariableSubtypeOfFunction(vs, processingContext, pureModel) ||
                                  (vs instanceof Collection && ((Collection) vs).values.stream().allMatch(v -> v instanceof LambdaFunction || (v instanceof ClassInstance && ((ClassInstance) v).type.equals("path")) || isVariableSubtypeOfFunction(v, processingContext, pureModel)));
        boolean isParamEmpty = vs instanceof Collection && ((Collection) vs).values.isEmpty();
        return isParamEmpty || (isSignatureFunction && isParamFunction) || (!isSignatureFunction && !isParamFunction);
    }

    private boolean isPackageableElementSubtypeOfFunction(org.finos.legend.engine.protocol.pure.m3.valuespecification.ValueSpecification valueSpecification, ProcessingContext processingContext, PureModel pureModel)
    {
        return (valueSpecification instanceof PackageableElementPtr
                    && pureModel.getExecutionSupport().getProcessorSupport().package_getByUserPath(((PackageableElementPtr) valueSpecification).fullPath) != null
                    && Type.subTypeOf(
                            pureModel.getExecutionSupport().getProcessorSupport().package_getByUserPath(((PackageableElementPtr) valueSpecification).fullPath).getClassifier(),
                            pureModel.getExecutionSupport().getProcessorSupport().package_getByUserPath(M3Paths.Function),
                            pureModel.getExecutionSupport().getProcessorSupport()
                    )
                );
    }

    private boolean isVariableSubtypeOfFunction(org.finos.legend.engine.protocol.pure.m3.valuespecification.ValueSpecification valueSpecification, ProcessingContext processingContext, PureModel pureModel)
    {
        return (valueSpecification instanceof Variable
                && processingContext.getInferredVariable(((Variable) valueSpecification).name) != null
                // When variable is Nil, it says is subtype of function, so avoid that scenario
                && !Type.isBottomType(processingContext.getInferredVariable(((Variable) valueSpecification).name)._genericType()._rawType(), pureModel.getExecutionSupport().getProcessorSupport())
                && Type.subTypeOf(processingContext.getInferredVariable(((Variable) valueSpecification).name)._genericType()._rawType(), pureModel.getType("meta::pure::metamodel::function::Function"), pureModel.getExecutionSupport().getProcessorSupport()));
    }

    public abstract MutableList<FunctionHandler> handlers();

    // Methods to get and insert function handler at certain coordinate within the function expression builder

    public abstract void insertFunctionHandlerAtIndex(int idx, FunctionHandler functionHandler);

    public abstract FunctionExpressionBuilder getFunctionExpressionBuilderAtIndex(int idx);

    public abstract void insertFunctionExpressionBuilderAtIndex(int idx, FunctionExpressionBuilder functionExpressionBuilder);

    public abstract void addFunctionHandler(FunctionHandler functionHandler);

    public abstract Boolean supportFunctionHandler(FunctionHandler handler);

    public abstract Optional<Integer> getParametersSize();
}
