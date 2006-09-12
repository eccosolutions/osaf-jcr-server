/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  The ASF licenses this file to You
 * under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.jackrabbit.webdav.jcr.nodetype;

import org.apache.log4j.Logger;
import org.apache.jackrabbit.webdav.xml.DomUtil;
import org.w3c.dom.Element;
import org.w3c.dom.Document;

import javax.jcr.nodetype.PropertyDefinition;
import javax.jcr.Value;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;

/**
 * <code>PropertyDefinitionImpl</code>...
 */
public final class PropertyDefinitionImpl extends ItemDefinitionImpl implements PropertyDefinition {

    private static Logger log = Logger.getLogger(PropertyDefinitionImpl.class);

    private final int type;
    private final String[] valueConstraints;
    private final Value[] defaultValues;
    private final boolean isMultiple;

    private PropertyDefinitionImpl(PropertyDefinition definition) {
	super(definition);

	type = definition.getRequiredType();
	valueConstraints = definition.getValueConstraints();
	defaultValues = definition.getDefaultValues();
	isMultiple = definition.isMultiple();
    }

    public static PropertyDefinitionImpl create(PropertyDefinition definition) {
	if (definition instanceof PropertyDefinitionImpl) {
	    return (PropertyDefinitionImpl)definition;
	} else {
	    return new PropertyDefinitionImpl(definition);
	}
    }

    //----------------------------------------< PropertyDefintion interface >---
    /**
     * @see PropertyDefinition#getRequiredType()
     */
    public int getRequiredType() {
	return type;
    }

    /**
     * @see PropertyDefinition#getValueConstraints()
     */
    public String[] getValueConstraints() {
	return valueConstraints;
    }

    /**
     * @see PropertyDefinition#getDefaultValues()
     */
    public Value[] getDefaultValues() {
	return defaultValues;
    }

    /**
     * @see PropertyDefinition#isMultiple()
     */
    public boolean isMultiple() {
	return isMultiple;
    }

    //-------------------------------------< implementation specific method >---
    /**
     * Return xml representation
     *
     * @return xml representation
     * @param document
     */
    public Element toXml(Document document) {
	Element elem = super.toXml(document);

        elem.setAttribute(MULTIPLE_ATTRIBUTE, Boolean.toString(isMultiple()));
        elem.setAttribute(REQUIREDTYPE_ATTRIBUTE, PropertyType.nameFromValue(getRequiredType()));

        // default values may be 'null'
        Value[] values = getDefaultValues();
        if (values != null) {
            Element dvElement = document.createElement(DEFAULTVALUES_ELEMENT);
            for (int i = 0; i < values.length; i++) {
                try {
                    Element valElem = document.createElement(DEFAULTVALUE_ELEMENT);
                    DomUtil.setText(valElem, values[i].getString());
                    dvElement.appendChild(valElem);
                } catch (RepositoryException e) {
                    // should not occur
                    log.error(e.getMessage());
                }
            }
            elem.appendChild(dvElement);
        }
        // value constraints array is never null.
        Element constrElem = document.createElement(VALUECONSTRAINTS_ELEMENT);
        String[] constraints = getValueConstraints();
        for (int i = 0; i < constraints.length; i++) {
            Element vcElem = document.createElement(VALUECONSTRAINT_ELEMENT);
            DomUtil.setText(vcElem, constraints[i]);
            constrElem.appendChild(vcElem);
        }
        elem.appendChild(constrElem);

        return elem;
    }

    /**
     * Returns {@link #PROPERTYDEFINITION_ELEMENT}.
     * 
     * @return always returns {@link #PROPERTYDEFINITION_ELEMENT}
     */
    String getElementName() {
	return PROPERTYDEFINITION_ELEMENT;
    }
}