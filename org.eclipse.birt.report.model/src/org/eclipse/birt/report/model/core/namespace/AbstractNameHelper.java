/*******************************************************************************
 * Copyright (c) 2004 Actuate Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *  Actuate Corporation  - initial API and implementation
 *******************************************************************************/

package org.eclipse.birt.report.model.core.namespace;

import java.util.List;

import org.eclipse.birt.report.model.api.core.IAccessControl;
import org.eclipse.birt.report.model.api.elements.ReportDesignConstants;
import org.eclipse.birt.report.model.api.metadata.IElementDefn;
import org.eclipse.birt.report.model.api.metadata.IPropertyType;
import org.eclipse.birt.report.model.api.metadata.MetaDataConstants;
import org.eclipse.birt.report.model.api.util.StringUtil;
import org.eclipse.birt.report.model.core.DesignElement;
import org.eclipse.birt.report.model.core.Module;
import org.eclipse.birt.report.model.core.NameSpace;
import org.eclipse.birt.report.model.core.StyleElement;
import org.eclipse.birt.report.model.elements.Library;
import org.eclipse.birt.report.model.elements.ReportDesign;
import org.eclipse.birt.report.model.i18n.ModelMessages;
import org.eclipse.birt.report.model.metadata.ElementDefn;
import org.eclipse.birt.report.model.metadata.ElementPropertyDefn;
import org.eclipse.birt.report.model.metadata.ElementRefValue;
import org.eclipse.birt.report.model.metadata.MetaDataDictionary;
import org.eclipse.birt.report.model.metadata.NamePropertyType;
import org.eclipse.birt.report.model.metadata.PropertyDefn;
import org.eclipse.birt.report.model.util.LevelContentIterator;

/**
 * 
 */
abstract public class AbstractNameHelper implements INameHelper, IAccessControl
{

	/**
	 * The array of the name contexts.
	 */

	protected INameContext nameContexts[] = null;

	/**
	 * The array of name spaces to cache some element names. The meanings of
	 * each name space is defined in
	 * {@link org.eclipse.birt.report.model.api.metadata.MetaDataConstants
	 * MetaDataConstants}.
	 */

	protected NameSpace cachedNameSpaces[] = null;

	/**
	 * 
	 */
	public AbstractNameHelper( )
	{
		// init cached name spaces
		initCachedNameSpaces( );
	}

	/**
	 * 
	 */
	protected void initCachedNameSpaces( )
	{
		cachedNameSpaces = new NameSpace[getNameSpaceCount( )];
		for ( int i = 0; i < getNameSpaceCount( ); i++ )
		{
			cachedNameSpaces[i] = new NameSpace( );
		}
	}

	/**
	 * Initializes all the name context and name manager of this helper.
	 */
	abstract protected void initialize( );

	/**
	 * 
	 * @return count of all the name space
	 */
	protected abstract int getNameSpaceCount( );

	/**
	 * Gets the name context with the given id.
	 * 
	 * @param nameSpaceID
	 * @return the name context for specified id
	 */
	public INameContext getNameContext( int nameSpaceID )
	{
		if ( nameSpaceID >= 0 && nameSpaceID < getNameSpaceCount( ) )
			return this.nameContexts[nameSpaceID];
		return new DummyNameContext( );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.birt.report.model.api.core.INameManager#flush()
	 */

	public void clear( )
	{
		initCachedNameSpaces( );
	}

	/**
	 * Gets the cached namespace with the given id.
	 * 
	 * @param id
	 *            the namespace id to get
	 * @return the cached namespace with the given id
	 */

	public NameSpace getCachedNameSpace( int id )
	{
		assert id >= 0 && id < getNameSpaceCount( );
		return cachedNameSpaces[id];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.birt.report.model.core.namespace.INameHelper#dropElement(
	 * org.eclipse.birt.report.model.core.DesignElement)
	 */
	public final void dropElement( DesignElement element )
	{
		if ( element == null )
			return;

		ElementDefn defn = (ElementDefn) element.getDefn( );
		int id = defn.getNameSpaceID( );
		NameSpace ns = getCachedNameSpace( id );

		String name = element.getName( );
		if ( element instanceof StyleElement )
			name = name == null ? null : name.toLowerCase( );
		if ( ns.getElement( name ) == element )
			ns.remove( element );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.birt.report.model.core.namespace.INameHelper#rename(org.eclipse
	 * .birt.report.model.core.DesignElement)
	 */
	public final void rename( DesignElement element )
	{
		if ( element == null )
			return;

		ElementDefn defn = (ElementDefn) element.getDefn( );

		if ( defn.getNameOption( ) == MetaDataConstants.REQUIRED_NAME
				|| element.getRoot( ) instanceof Library
				|| getElement( ).getRoot( ) instanceof Library
				|| element.getName( ) != null )
		{
			// if element holder is just this
			if ( getElement( ).getDefn( ).isKindOf(
					defn.getNameConfig( ).getNameContainer( ) ) )
				makeUniqueName( element );
			else
			{
				INameHelper nameHelper = new NameExecutor( element )
						.getNameHelper( getElement( ).getRoot( ) );
				if ( nameHelper != null )
					nameHelper.makeUniqueName( element );
			}
		}

		LevelContentIterator iter = new LevelContentIterator( getElement( )
				.getRoot( ), element, 1 );
		while ( iter.hasNext( ) )
		{
			DesignElement innerElement = iter.next( );
			rename( innerElement );
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.birt.report.model.core.namespace.INameHelper#getNameSpace
	 * (int)
	 */
	public NameSpace getNameSpace( int nameSpaceID )
	{
		return getNameContext( nameSpaceID ).getNameSpace( );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.birt.report.model.core.namespace.INameHelper#makeUniqueName
	 * (org.eclipse.birt.report.model.core.DesignElement)
	 */
	public void makeUniqueName( DesignElement element )
	{
		if ( element == null )
			return;

		ElementDefn eDefn = (ElementDefn) element.getDefn( );
		if ( !getElement( ).getDefn( ).isKindOf(
				eDefn.getNameConfig( ).getNameContainer( ) ) )
		{
			INameHelper nameHelper = new NameExecutor( element )
					.getNameHelper( getElement( ).getRoot( ) );
			if ( nameHelper != null )
				nameHelper.makeUniqueName( element );
			return;
		}

		String name = getUniqueName( element );
		if ( name == null )
			return;

		// set the unique name and add the element to the name manager

		NameSpace nameSpace = getCachedNameSpace( eDefn.getNameSpaceID( ) );
		String validName = name;
		if ( element instanceof StyleElement )
			validName = validName.toLowerCase( );
		DesignElement cachedElement = nameSpace.getElement( validName );
		if ( cachedElement == null )
		{
			element.setName( name.trim( ) );
			nameSpace.insert( element );
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.birt.report.model.core.namespace.INameHelper#resolve(org.
	 * eclipse.birt.report.model.core.DesignElement, java.lang.String,
	 * org.eclipse.birt.report.model.metadata.PropertyDefn,
	 * org.eclipse.birt.report.model.api.metadata.IElementDefn)
	 */
	public ElementRefValue resolve( DesignElement focus, String elementName,
			PropertyDefn propDefn, IElementDefn elementDefn )
	{
		assert isValidReferenceProperty( propDefn );

		ElementDefn targetDefn = (ElementDefn) elementDefn;

		if ( targetDefn == null )
		{
			targetDefn = propDefn == null ? null : (ElementDefn) propDefn
					.getTargetElementType( );
		}

		ElementDefn nameContainerDefn = (ElementDefn) targetDefn
				.getNameConfig( ).getNameContainer( );
		// if name container is not found, then return null;
		assert nameContainerDefn != null;

		// if the current element is a valid name container for the target
		// definition type, then just call find and return
		if ( getElement( ).getDefn( ).isKindOf( nameContainerDefn ) )
		{
			int id = targetDefn.getNameSpaceID( );
			return getNameContext( id ).resolve( focus, elementName, propDefn,
					(ElementDefn) elementDefn );
		}

		// if the name container has no name, it must define a target property
		// in report design to find the name host
		if ( nameContainerDefn.getNameOption( ) == MetaDataConstants.NO_NAME )
		{
			return resolveNameInNonameHost( focus, elementName, propDefn,
					targetDefn, nameContainerDefn );
		}

		// the current element is not a valid name container for the target, we
		// will have to do the search by the root
		return resolveName( focus, elementName, propDefn, targetDefn );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.birt.report.model.core.namespace.INameHelper#resolve(org.
	 * eclipse.birt.report.model.core.DesignElement,
	 * org.eclipse.birt.report.model.core.DesignElement,
	 * org.eclipse.birt.report.model.metadata.PropertyDefn,
	 * org.eclipse.birt.report.model.api.metadata.IElementDefn)
	 */
	public ElementRefValue resolve( DesignElement focus, DesignElement element,
			PropertyDefn propDefn, IElementDefn elementDefn )
	{
		if ( element == null )
			return null;

		if ( !isValidReferenceProperty( propDefn ) )
			throw new IllegalArgumentException(
					"Property should be element reference type or extends type" ); //$NON-NLS-1$

		ElementDefn targetDefn = propDefn == null
				? null
				: (ElementDefn) propDefn.getTargetElementType( );
		if ( targetDefn == null )
		{
			if ( elementDefn == null )
				throw new IllegalArgumentException(
						"The element definition should not be null" ); //$NON-NLS-1$
			targetDefn = (ElementDefn) elementDefn;
		}

		// if element is not a valid target type, then return directly
		if ( !element.getDefn( ).isKindOf( targetDefn ) )
		{
			Module module = element.getRoot( );
			String namespace = module == null ? null : module.getNamespace( );
			String name = element.getFullName( );
			return new ElementRefValue( namespace, name );
		}

		ElementDefn nameContainerDefn = (ElementDefn) targetDefn
				.getNameConfig( ).getNameContainer( );
		// if name container is not found, then return null;
		assert nameContainerDefn != null;

		// if the current element is a valid name container for the target
		// definition type, then just call find and return
		if ( getElement( ).getDefn( ).isKindOf( nameContainerDefn ) )
		{
			int id = targetDefn.getNameSpaceID( );
			return getNameContext( id ).resolve( focus, element, propDefn,
					(ElementDefn) elementDefn );
		}

		// if the name container has no name, it must define a target property
		// in report design to find the name host
		if ( nameContainerDefn.getNameOption( ) == MetaDataConstants.NO_NAME )
		{
			// TODO
		}

		// the current element is not a valid name container for the target, we
		// will have to do the search by the root
		return resolveElement( focus, element, propDefn, targetDefn );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.birt.report.model.core.namespace.INameHelper#canContain(int,
	 * java.lang.String)
	 */
	public boolean canContain( int nameSpaceID, String elementName )
	{
		return getNameContext( nameSpaceID ).canContain( elementName );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.birt.report.model.core.namespace.INameHelper#getElements(int,
	 * int)
	 */
	public List<DesignElement> getElements( int nameSpaceID, int level )
	{
		return getNameContext( nameSpaceID ).getElements( level );
	}

	/**
	 * Gets the root resolve information elementName/elementDefn pair.
	 * 
	 * @param elementName
	 * @param targetDefn
	 * @return the root resolve information elementName/elementDefn pair
	 */
	private NameResolveInfor getRootResolveInfor( String elementName,
			ElementDefn targetDefn )
	{
		NameResolveInfor searchInfor = new NameResolveInfor( targetDefn,
				elementName );

		IElementDefn moduleDefn = MetaDataDictionary.getInstance( ).getElement(
				ReportDesignConstants.MODULE_ELEMENT );
		while ( searchInfor != null )
		{
			ElementDefn holderDefn = (ElementDefn) searchInfor.elementDefn
					.getNameConfig( ).getNameContainer( );
			if ( holderDefn != null && holderDefn.isKindOf( moduleDefn ) )
			{
				return searchInfor;
			}

			int index = searchInfor.elementName
					.lastIndexOf( NameExecutor.NAME_SEPARATOR );
			String resolveName = index == -1
					? searchInfor.elementName
					: searchInfor.elementName.substring( 0, index );

			targetDefn = searchInfor.elementDefn;
			searchInfor = holderDefn == null ? null : new NameResolveInfor(
					holderDefn, resolveName );
		}

		return null;
	}

	/**
	 * Resolves the element.
	 * 
	 * @param element
	 * @param propDefn
	 * @return the element reference value
	 */
	private ElementRefValue resolveElement( DesignElement focus,
			DesignElement element, PropertyDefn propDefn,
			IElementDefn elementDefn )
	{
		assert element != null;
		INameHelper nameHelper = new NameExecutor( element )
				.getNameHelper( getElement( ).getRoot( ) );
		return nameHelper == null ? null : nameHelper.resolve( focus, element,
				propDefn, elementDefn );
	}

	private ElementRefValue resolveNameInNonameHost( DesignElement focus,
			String elementName, PropertyDefn propDefn, ElementDefn elementDefn,
			IElementDefn nameContainerDefn )
	{
		assert elementName != null;
		assert elementDefn != null;
		assert nameContainerDefn != null;
		Module root = getElement( ).getRoot( );

		if ( root != null && root instanceof ReportDesign )
		{
			ElementPropertyDefn targetProperty = (ElementPropertyDefn) elementDefn
					.getNameConfig( ).getNameProperty( );
			if ( targetProperty != null )
			{
				Object value = root.getProperty( root, targetProperty );
				if ( value instanceof DesignElement )
				{
					assert value instanceof INameContainer;
					return ( (INameContainer) value ).getNameHelper( ).resolve(
							focus, elementName, propDefn, elementDefn );
				}
				else if ( value instanceof List )
				{
					List valueList = (List) value;
					if ( !valueList.isEmpty( ) )
					{
						Object item = valueList.get( 0 );
						assert item instanceof INameContainer;
						return ( (INameContainer) item ).getNameHelper( )
								.resolve( focus, elementName, propDefn,
										elementDefn );
					}
				}
			}
		}

		return new ElementRefValue( StringUtil.extractNamespace( elementName ),
				StringUtil.extractName( elementName ) );
	}

	/**
	 * Resolves the element name.
	 * 
	 * @param elementName
	 * @param propDefn
	 * @return the element name.
	 */
	private ElementRefValue resolveName( DesignElement focus,
			String elementName, PropertyDefn propDefn, ElementDefn elementDefn )
	{
		assert elementName != null;
		assert elementDefn != null;
		Module root = getElement( ).getRoot( );

		if ( root != null )
		{
			NameResolveInfor rootInfor = getRootResolveInfor( elementName,
					elementDefn );

			// if root infor is found, then do the resolve
			if ( rootInfor != null )
			{
				int id = rootInfor.elementDefn.getNameSpaceID( );
				AbstractNameHelper nameHelper = (AbstractNameHelper) root
						.getNameHelper( );
				ElementRefValue refValue = nameHelper.getNameContext( id )
						.resolve( focus, rootInfor.elementName, propDefn,
								rootInfor.elementDefn );
				if ( refValue != null && refValue.isResolved( ) )
				{
					DesignElement parentTarget = refValue.getElement( );
					assert parentTarget instanceof INameContainer;
					return ( (INameContainer) parentTarget )
							.getNameHelper( )
							.resolve( focus, elementName, propDefn, elementDefn );
				}
			}
		}

		// return unresolved, if root is not found
		return new ElementRefValue( StringUtil.extractNamespace( elementName ),
				StringUtil.extractName( elementName ) );

	}

	/**
	 * Validates whether this property definition can refer other element.
	 * 
	 * @param propDefn
	 * @return <true> if this property definition can refer other element,
	 *         otherwise return <false>.
	 */
	private boolean isValidReferenceProperty( PropertyDefn propDefn )
	{
		if ( propDefn == null )
			return true;
		int typeCode = propDefn.getTypeCode( );
		switch ( typeCode )
		{
			case IPropertyType.ELEMENT_REF_TYPE :
			case IPropertyType.EXTENDS_TYPE :
				return true;
			case IPropertyType.LIST_TYPE :
				return propDefn.getSubTypeCode( ) == IPropertyType.ELEMENT_REF_TYPE;
			default :
				return false;
		}
	}

	/**
	 * @param namespace
	 * @param element
	 * @param name
	 * @return true if the name is unique, otherwise false
	 */

	protected static boolean isValidInNameSpace( NameSpace namespace,
			DesignElement element, String name )
	{
		DesignElement tmpElement = namespace.getElement( name );
		if ( tmpElement == null || tmpElement == element )
			return true;

		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.birt.report.model.core.namespace.INameHelper#cacheValues()
	 */

	public void cacheValues( )
	{

	}

	private static class NameResolveInfor
	{

		ElementDefn elementDefn = null;
		String elementName = null;

		NameResolveInfor( ElementDefn defn, String name )
		{
			this.elementDefn = defn;
			this.elementName = name;
		}
	}

	/*
	 * 
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.birt.report.model.core.namespace.INameHelper#getUniqueName
	 * (org.eclipse.birt.report.model.core.DesignElement, java.lang.String)
	 */
	public String getUniqueName( DesignElement element, String namePrefix )
	{
		if ( element == null )
			return null;

		ElementDefn eDefn = (ElementDefn) element.getDefn( );

		// if the element does not reside in the dimension, then get namehelper
		// for the element and get unique name from there
		if ( !getElement( ).getDefn( ).isKindOf(
				eDefn.getNameConfig( ).getNameContainer( ) ) )
		{
			INameHelper nameHelper = new NameExecutor( element )
					.getNameHelper( getElement( ).getRoot( ) );
			return nameHelper == null ? null : nameHelper.getUniqueName(
					element, namePrefix );
		}

		String name = element.getName( );

		if ( StringUtil.isBlank( name ) )
		{
			// Use the given prefix if the element name is null
			name = namePrefix;
		}
		name = StringUtil.trimString( name );

		// replace all the illegal chars with '_'
		name = NamePropertyType.validateName( name );

		// Some elements can have a blank name.
		if ( eDefn.getNameOption( ) == MetaDataConstants.NO_NAME )
			return null;

		if ( eDefn.getNameOption( ) == MetaDataConstants.OPTIONAL_NAME
				&& name == null
				&& getElement( ).getRoot( ) instanceof ReportDesign )
			return null;

		// If the element already has a unique name, return it.
		int id = eDefn.getNameSpaceID( );
		NameSpace nameSpace = getCachedNameSpace( id );
		NameSpace moduleNameSpace = nameContexts[id].getNameSpace( );
		if ( name != null && isValidInNameSpace( nameSpace, element, name )
				&& isValidInNameSpace( moduleNameSpace, element, name ) )
			return name;

		// If the element has no name, create it as "New<new name>" where
		// "<new name>" is the new element display name for the element. Both
		// "New" and the new element display name are localized to the user's
		// locale.

		if ( name == null )
		{
			name = getDefaultName( element );
		}

		// Add a numeric suffix that makes the name unique.
		int index = 0;
		String baseName = name;
		while ( nameSpace.contains( name ) || moduleNameSpace.contains( name ) )
		{
			name = baseName + ++index;
		}

		return name;
	}

	/*
	 * 
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.birt.report.model.core.namespace.INameHelper#getUniqueName
	 * (org.eclipse.birt.report.model.core.DesignElement)
	 */
	public final String getUniqueName( DesignElement element )
	{
		return getUniqueName( element, null );
	}

	protected String getDefaultName( DesignElement element )
	{
		String name = null;

		name = ModelMessages.getMessage( "New." //$NON-NLS-1$
				+ element.getDefn( ).getName( ) );
		name = name.trim( );

		return name;
	}
}
