/*
 * Copyright (c) 2015 Villu Ruusmann
 *
 * This file is part of JPMML-SkLearn
 *
 * JPMML-SkLearn is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * JPMML-SkLearn is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with JPMML-SkLearn.  If not, see <http://www.gnu.org/licenses/>.
 */
package sklearn.preprocessing;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ContiguousSet;
import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.Range;
import org.dmg.pmml.DataType;
import org.dmg.pmml.OpType;
import org.jpmml.converter.BinaryFeature;
import org.jpmml.converter.Feature;
import org.jpmml.converter.ListFeature;
import org.jpmml.converter.ValueUtil;
import org.jpmml.converter.WildcardFeature;
import org.jpmml.sklearn.ClassDictUtil;
import org.jpmml.sklearn.FeatureMapper;
import sklearn.Transformer;
import sklearn.TypeUtil;

public class OneHotEncoder extends Transformer {

	public OneHotEncoder(String module, String name){
		super(module, name);
	}

	@Override
	public OpType getOpType(){
		return OpType.CATEGORICAL;
	}

	@Override
	public DataType getDataType(){
		List<? extends Number> values = getValues();

		return TypeUtil.getDataType(values, DataType.INTEGER);
	}

	@Override
	public List<Feature> encodeFeatures(List<String> ids, List<Feature> inputFeatures, FeatureMapper featureMapper){
		List<? extends Number> values = getValues();

		if(ids.size() != 1 || inputFeatures.size() != 1){
			throw new IllegalArgumentException();
		}

		Feature inputFeature = inputFeatures.get(0);

		List<String> categories = new ArrayList<>();

		List<Feature> features = new ArrayList<>();

		for(int i = 0; i < values.size(); i++){
			int value = ValueUtil.asInt(values.get(i));

			String category;

			if(inputFeature instanceof ListFeature){
				ListFeature listFeature = (ListFeature)inputFeature;

				category = listFeature.getValue(value);
			} else

			if(inputFeature instanceof WildcardFeature){
				category = ValueUtil.formatValue((Integer)value);
			} else

			{
				throw new IllegalArgumentException();
			}

			categories.add(category);

			features.add(new BinaryFeature(inputFeature.getName(), DataType.STRING, category));
		}

		featureMapper.updateValueSpace(inputFeature.getName(), categories);

		return features;
	}

	public List<? extends Number> getValues(){
		List<Integer> featureSizes = getFeatureSizes();

		if(featureSizes.size() != 1){
			throw new IllegalArgumentException();
		}

		Object numberOfValues = get("n_values");

		if(("auto").equals(numberOfValues)){
			return getActiveFeatures();
		}

		Integer featureSize = featureSizes.get(0);

		List<Integer> result = new ArrayList<>();
		result.addAll(ContiguousSet.create(Range.closedOpen(0, featureSize), DiscreteDomain.integers()));

		return result;
	}

	public List<? extends Number> getActiveFeatures(){
		return (List)ClassDictUtil.getArray(this, "active_features_");
	}

	public List<Integer> getFeatureSizes(){
		return ValueUtil.asIntegers((List)ClassDictUtil.getArray(this, "n_values_"));
	}
}