/*******************************************************************************
 * Copyright (c) 2004, 2007 Actuate Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *  Actuate Corporation  - initial API and implementation
 *******************************************************************************/

package org.eclipse.birt.chart.ui.swt.wizard.format.popup;

import org.eclipse.birt.chart.ui.swt.composites.TriggerDataComposite;
import org.eclipse.birt.chart.ui.swt.wizard.ChartWizardContext;
import org.eclipse.birt.chart.ui.util.ChartHelpContextIds;
import org.eclipse.birt.chart.ui.util.ChartUIUtil;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;

/**
 * 
 */

public class InteractivitySheet extends AbstractPopupSheet
{

	private final EList triggers;
	private final EObject cursorContainer;
	private final boolean bEnableURLParameters;
	private final boolean bEnableShowTooltipValue;
	private final int iInteractivityType;
	private int optionalStyle;

	/**
	 * 
	 * @param title
	 * @param context
	 * @param triggers
	 * @param iInteractivityType
	 *            see <code>TriggerSupportMatrix</code>
	 * @param bEnableURLParameters
	 * @param bEnableShowTooltipValue
	 */
	public InteractivitySheet( String title, ChartWizardContext context,
			EList triggers, EObject cursorContainer, int iInteractivityType, int optionalStyle )
	{
		this( title,
				context,
				triggers,
				cursorContainer,
				iInteractivityType,
				( ( optionalStyle & TriggerDataComposite.ENABLE_URL_PARAMETERS ) == TriggerDataComposite.ENABLE_URL_PARAMETERS ),
				( ( optionalStyle & TriggerDataComposite.ENABLE_SHOW_TOOLTIP_VALUE ) == TriggerDataComposite.ENABLE_SHOW_TOOLTIP_VALUE ) );
		this.optionalStyle = optionalStyle;
	}

	/**
	 * 
	 * @param title
	 * @param context
	 * @param triggers
	 * @param iInteractivityType
	 *            see <code>TriggerSupportMatrix</code>
	 * @param bEnableURLParameters
	 * @param bEnableShowTooltipValue
	 */
	public InteractivitySheet( String title, ChartWizardContext context,
			EList triggers, EObject cursorContainer, int iInteractivityType,
			boolean bEnableURLParameters, boolean bEnableShowTooltipValue )
	{
		super( title, context, false );
		this.triggers = triggers;
		this.cursorContainer = cursorContainer;
		this.bEnableURLParameters = bEnableURLParameters;
		this.bEnableShowTooltipValue = bEnableShowTooltipValue;
		this.iInteractivityType = iInteractivityType;
		if ( bEnableShowTooltipValue )
		{
			optionalStyle |= TriggerDataComposite.ENABLE_SHOW_TOOLTIP_VALUE;
		}
		if ( bEnableURLParameters )
		{
			optionalStyle |= TriggerDataComposite.ENABLE_URL_PARAMETERS;
		}
	}

	protected Composite getComponent( Composite parent )
	{
		ChartUIUtil.bindHelp( parent, ChartHelpContextIds.POPUP_INTERACTIVITY );
		final TriggerDataComposite triggerUI = new TriggerDataComposite( parent,
				SWT.NONE,
				triggers,
				cursorContainer,
				getContext( ),
				iInteractivityType,
				optionalStyle );
		parent.getShell( ).addDisposeListener( new DisposeListener( ) {

			public void widgetDisposed( DisposeEvent e )
			{
				triggerUI.markSaveWhenClosing( );
			}
		} );
		return triggerUI;
	}

}
