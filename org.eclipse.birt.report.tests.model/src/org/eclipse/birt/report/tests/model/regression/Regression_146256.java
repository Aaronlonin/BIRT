/*******************************************************************************
 * Copyright (c) 2004 Actuate Corporation. All rights reserved. This program and
 * the accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html Contributors: Actuate Corporation -
 * initial API and implementation
 ******************************************************************************/

package org.eclipse.birt.report.tests.model.regression;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import org.eclipse.birt.report.model.api.IResourceLocator;
import org.eclipse.birt.report.model.util.ResourceLocatorImpl;
import org.eclipse.birt.report.tests.model.BaseTestCase;

/**
 * <b>Regression description:</b>
 * <p>
 * Birt cannot process images from a jar-based plugin
 * <p>
 * <b>Test description:</b>
 * <p>
 * Test the resource locator given a jar protocal with an image file.
 * <p>
 */
public class Regression_146256 extends BaseTestCase
{

	/**
	 * Tests the 'findFile' method of DefaultSearchFileAlgorithm.
	 * 
	 * @throws Exception
	 *             if the test fails.
	 */

	public void setUp( ) throws Exception
	{
		super.setUp( );
		removeResource( );
		//copyResource_INPUT( "testRead.jar" , "testRead.jar" );

		copyInputToFile ( INPUT_FOLDER + "/" + "testRead.jar" );
	}
	
	public void tearDown( )
	{
		removeResource( );
	}
	
	public void test_regression_146256( ) throws Exception
	{

		ResourceLocatorImpl rl = new ResourceLocatorImpl( );
		//String resource = "jar:file:" + this.getFullQualifiedClassName( ) + "/" + INPUT_FOLDER + "/" + "testRead.jar!/test/testRead.rptdesign"; //$NON-NLS-1$
		String resource = "jar:file:" + getTempFolder() + "/" + INPUT_FOLDER + "/" + "testRead.jar!/test/testRead.rptdesign"; //$NON-NLS-1$
		
		
		URL url = rl.findResource( null, resource, IResourceLocator.IMAGE );
		assertNotNull( url );

		URLConnection jarConnection = url.openConnection( );
		jarConnection.connect( );

		InputStream inputStream = jarConnection.getInputStream( );
		assertNotNull( inputStream );
	}
}
