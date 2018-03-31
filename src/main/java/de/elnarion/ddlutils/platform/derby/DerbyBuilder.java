package de.elnarion.ddlutils.platform.derby;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.IOException;
import java.sql.Types;

import de.elnarion.ddlutils.Platform;
import de.elnarion.ddlutils.alteration.ColumnDefinitionChange;
import de.elnarion.ddlutils.model.Column;
import de.elnarion.ddlutils.model.Index;
import de.elnarion.ddlutils.model.Table;
import de.elnarion.ddlutils.model.TypeMap;
import de.elnarion.ddlutils.platform.SqlBuilder;

/**
 * The SQL Builder for Derby.
 * 
 * @version $Revision: 279413 $
 */
public class DerbyBuilder extends SqlBuilder
{
    /**
     * Creates a new builder instance.
     * 
     * @param platform The plaftform this builder belongs to
     */
    public DerbyBuilder(Platform platform)
    {
    	super(platform);
        addEscapedCharSequence("'", "''");
    }


    /**
     * {@inheritDoc}
     */
    public String getSelectLastIdentityValues(Table table)
    {
        return "VALUES IDENTITY_VAL_LOCAL()";
    }
    
    /**
     * {@inheritDoc}
     */
    protected String getNativeDefaultValue(Column column)
    {
        if ((column.getTypeCode() == Types.BIT) || (column.getTypeCode() == Types.BOOLEAN))
        {
            return getDefaultValueHelper().convert(column.getDefaultValue(), column.getTypeCode(), Types.SMALLINT);
        }
        else
        {
            return super.getNativeDefaultValue(column);
        }
    }

    /**
     * {@inheritDoc}
     */
    protected void writeColumnAutoIncrementStmt(Table table, Column column) throws IOException
    {
        print("GENERATED BY DEFAULT AS IDENTITY");
    }

    /**
     * {@inheritDoc}
     */
    public void dropIndex(Table table, Index index) throws IOException
    {
        // Index names in Derby are unique to a schema and hence Derby does not
        // use the ON <tablename> clause
        print("DROP INDEX ");
        printIdentifier(getIndexName(index));
        printEndOfStatement();
    }

    /**
     * {@inheritDoc}
     */
    protected void writeCastExpression(Column sourceColumn, Column targetColumn) throws IOException
    {
        if (ColumnDefinitionChange.isSizeChanged(getPlatformInfo(), sourceColumn, targetColumn) ||
            ColumnDefinitionChange.isTypeChanged(getPlatformInfo(), sourceColumn, targetColumn))
        {
            String targetNativeType = getNativeType(targetColumn);

            // Derby currently has the limitation that it cannot convert numeric values
            // to VARCHAR, though it can convert them to CHAR
            if (TypeMap.isNumericType(sourceColumn.getTypeCode()) &&
                "VARCHAR".equalsIgnoreCase(targetNativeType))
            {
                targetNativeType = "CHAR";
            }

            print("CAST (");
            printIdentifier(getColumnName(sourceColumn));
            print(" AS ");
            print(getSqlType(targetColumn, targetNativeType));
            print(")");
        }
        else
        {
            printIdentifier(getColumnName(sourceColumn));
        }
    }
}