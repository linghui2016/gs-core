/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place - Suite 330, Boston, MA 02111-1307, USA.
 * 
 * Copyright 2006 - 2009
 * 	Julien Baudry
 * 	Antoine Dutot
 * 	Yoann Pigné
 * 	Guilhelm Savin
 */

package org.miv.graphstream.ui2.spriteManager;

import java.util.HashMap;
import java.util.Iterator;

import org.miv.graphstream.graph.Graph;
import org.miv.graphstream.graph.GraphAttributesListener;
import org.miv.graphstream.ui2.graphicGraph.stylesheet.Value;
import org.miv.graphstream.ui2.graphicGraph.stylesheet.Style;
import org.miv.graphstream.ui2.graphicGraph.stylesheet.Values;
import org.miv.graphstream.ui2.graphicGraph.stylesheet.StyleConstants.Units;

/**
 * Set of sprites associated with a graph.
 * 
 * <p>
 * The sprite manager acts as a set of sprite elements that are associated with a graph. There can
 * be only one sprite manager per graph. The sprite manager only role is to allow to create,
 * destroy and enumerate sprites of a graph.
 * </p>
 * 
 * <p>
 * See the {@link Sprite} class for an explanation of what are sprites and how to use them. 
 * </p>
 * 
 * <p>
 * In case you need to refine the Sprite class, you can change the {@link SpriteFactory} of this
 * manager so that it creates specific instances of sprites instead of the default ones.
 * </p>
 */
public class SpriteManager implements Iterable<Sprite>, GraphAttributesListener
{
// Attribute
	
	/**
	 * The graph to add sprites to.
	 */
	protected Graph graph;
	
	/**
	 * The set of sprites.
	 */
	protected HashMap<String,Sprite> sprites = new HashMap<String,Sprite>();
	
	/**
	 * Factory to create new sprites.
	 */
	protected SpriteFactory factory = new SpriteFactory();

// Attributes
	
	/**
	 * this acts as a lock when we are adding a sprite since we are also listener of the
	 * graph, and when we receive an "add" event, we automatically create a sprite. We can
	 * want to avoid listening at ourself.
	 */
	boolean attributeLock = false;
	
// Construction
	
	/**
	 * Create a new manager for sprite and bind it to the given graph. If the graph already contains
	 * attributes describing sprites, the manager is automatically filled with the existing
	 * sprites.
	 * @param graph The graph to associate with this manager;
	 */
	public SpriteManager( Graph graph )
	{
		this.graph    = graph;
		
		lookForExistingSprites();
		graph.addGraphAttributesListener( this );
	}
	
	protected void lookForExistingSprites()
	{
		for( String attr: graph.getAttributeKeySet() )
		{
			if( attr.startsWith( "ui.sprite." ) )
			{
				String id = attr.substring( 10 );
				
				if( id.indexOf( '.' ) < 0 )
				{
					addSprite( id );
				}
			}
		}
	}

// Access
	
	/**
	 * Number of sprites in the manager.
	 * @return The sprite count.
	 */
	public int getSpriteCount()
	{
		return sprites.size();
	}
	
	/**
	 * True if the manager contains a sprite corresponding to the given identifier.
	 * @param identifier The sprite identifier to search for.
	 */
	public boolean hasSprite( String identifier )
	{
		return( sprites.get( identifier ) != null );
	}
	
	/**
	 * Sprite corresponding to the given identifier or null if no sprite is associated with the
	 * given identifier.
	 * @param identifier The sprite identifier.
	 */
	public Sprite getSprite( String identifier )
	{
		return sprites.get( identifier );
	}
	
	/**
	 * Iterable set of sprites in no particular order.
	 * @return The set of sprites.
	 */
	public Iterable<? extends Sprite> sprites()
	{
		return sprites.values();
	}
	
	/**
	 * Iterator on the set of sprites.
	 * @return An iterator on sprites.
	 */
	public Iterator<? extends Sprite> spriteIterator()
	{
		return sprites.values().iterator();
	}
	
	/**
	 * Iterator on the set of sprites.
	 * @return An iterator on sprites.
	 */
	public Iterator<Sprite> iterator()
	{
		return sprites.values().iterator();
	}
	
	/**
	 * The current sprite factory.
	 * @return A Sprite factory.
	 */
	public SpriteFactory getSpriteFactory()
	{
		return factory;
	}
	
// Command

	/**
	 * Detach this manager from its graph. This manager will no more be usable to create or remove
	 * sprites. However sprites not yet removed are still present as attributes in the graph and
	 * binding another sprite manager to this graph will retrieve all sprites. 
	 */
	public void detach()
	{
		graph.removeGraphAttributesListener( this );
		sprites.clear();
		
		graph = null;
	}
	
	/**
	 * Specify the sprite factory to use. This allows to use specific sprite classes (descendants
	 * of Sprite).
	 * @param factory The new factory to use.
	 */
	public void setSpriteFactory( SpriteFactory factory )
	{
		this.factory = factory;
	}
	
	/**
	 * Reset the sprite factory to defaults.
	 */
	public void resetSpriteFactory()
	{
		factory = new SpriteFactory();
	}
	
	/**
	 * Add a sprite with the given identifier. If the sprite already exists, nothing is done.
	 * @param identifier The identifier of the new sprite to add.
	 * @return The created sprite.
	 */
	public Sprite addSprite( String identifier )
	{
		return addSprite( identifier, null );
	}
	
	/**
	 * Add a sprite with the given identifier and position. If the sprite already exists, nothing
	 * is done, excepted if the position is not null in which case it is repositioned. If the
	 * sprite does not exists, it is added and if position is not null, it is used as the initial
	 * position of the sprite. 
	 * @param identifier The sprite identifier.
	 * @param position The sprite position (or null for (0,0,0)).
	 * @return The created sprite.
	 */
	protected Sprite addSprite( String identifier, Values position )
	{
		Sprite sprite = sprites.get( identifier ); 
		
		if( sprite == null )
		{
			attributeLock = true;
			sprite = factory.newSprite( identifier, this, position );
			sprites.put( identifier, sprite );
			attributeLock = false;
		}
		else
		{
			if( position != null )
				sprite.setPosition( position );
		}
		
		return sprite;		
	}
	
	/**
	 * Remove a sprite knowing its identifier. If no such sprite exists, this fails silently.
	 * @param identifier The identifier of the sprite to remove.
	 */
	public void removeSprite( String identifier )
	{
		Sprite sprite = sprites.get( identifier ); 
		
		if( sprite != null )
		{
			attributeLock = true;
			sprites.remove( identifier );
			sprite.removed();
			attributeLock = false;
		}		
	}
	
// Utility
	
	protected static Values getPositionValue( Object value )
	{
		if( value instanceof Object[] )
		{
			Object[] values = (Object[]) value;
			
			if( values.length == 4 )
			{
				if( values[0] instanceof Number && values[1] instanceof Number
				 && values[2] instanceof Number && values[3] instanceof Style.Units )
				{
					return new Values(
							(Style.Units)values[3],
							((Number)values[0]).floatValue(),
							((Number)values[1]).floatValue(),
							((Number)values[2]).floatValue() );					
				}
				else
				{
					System.err.printf( "GraphicGraph : cannot parse values[4] for sprite position.%n" );
				}
			}
			else if( values.length == 3 )
			{
				if( values[0] instanceof Number && values[1] instanceof Number
				 && values[2] instanceof Number )
				{
					return new Values(
						Units.GU,
						((Number)values[0]).floatValue(),
						((Number)values[1]).floatValue(),
						((Number)values[2]).floatValue() );
				}
				else
				{
					System.err.printf( "GraphicGraph : cannot parse values[3] for sprite position.%n" );
				}
			}
			else if( values.length == 1 )
			{
				if( values[0] instanceof Number )
				{
					return new Values( Units.GU, ((Number)value).floatValue() );
				}
				else
				{
					System.err.printf( "GraphicGraph : sprite position percent is not a number.%n" );
				}
			}
			else
			{
				System.err.printf( "GraphicGraph : cannot transform value '%s' (length=%d) into a position%n", values, values.length );
			}
		}
		else if( value instanceof Number )
		{
			return new Values( Units.GU, ((Number)value).floatValue() );
		}
		else if( value instanceof Value )
		{
			return new Values( (Value)value );
		}
		else if( value instanceof Values )
		{
			return new Values( (Values)value );
		}
		else
		{
			System.err.printf( "GraphicGraph : cannot place sprite with posiiton '%s' (instance of %s)%n", value,
					value.getClass().getName() );
		}
		
		return null;
	}

// GraphAttributesListener
	
	public void graphAttributeAdded( String graphId, String attribute, Object value )
    {
		if( attributeLock )
			return;		// We want to avoid listening at ourselves.
		
		if( attribute.startsWith( "ui.sprite." ) )
		{
			String spriteId = attribute.substring( 10 );

			if( spriteId.indexOf( '.' ) < 0 )
			{
				if( getSprite( spriteId ) == null )
				{
					// A sprite has been created by another entity.
					// Synchronise this manager.
					
					Values position = null;
					
					if( value != null )
						position = getPositionValue( value );
		
					addSprite( spriteId, position );
				}
			}
		}
    }
	
	public void graphAttributeChanged( String graphId, String attribute, Object oldValue,
            Object newValue )
    {
		if( attributeLock )
			return;		// We want to avoid listening at ourselves.

		if( attribute.startsWith( "ui.sprite." ) )
		{
			String spriteId = attribute.substring( 10 );

			if( spriteId.indexOf( '.' ) < 0 )
			{
				Sprite s = getSprite( spriteId );
				
				if( s != null )
				{
					// The sprite has been moved by another entity.
					// Update its position.

					if( newValue != null )
					{
						Values position = getPositionValue( newValue );
//System.err.printf( "       %%spriteMan set %s Position(%s) (from %s)%n", spriteId, position, newValue );
						s.setPosition( position );
					}
					else
					{
						System.err.printf( "%s changed but newValue == null ! (old=%s)%n", spriteId, oldValue );
					}
				}
				else
				{
					throw new RuntimeException( "WTF ! sprite changed, but not added...%n" );
				}
			}
		}
    }

	public void graphAttributeRemoved( String graphId, String attribute )
    {
		if( attributeLock )
			return;		// We want to avoid listening at ourselves.
		
		if( attribute.startsWith( "ui.sprite." ) )
		{
			String spriteId = attribute.substring( 10 );

			if( spriteId.indexOf( '.' ) < 0 )
			{
				if( getSprite( spriteId ) != null )
				{
					// A sprite has been removed by another entity.
					// Synchronise this manager.
					
					removeSprite( spriteId );
				}
			}
		}
    }

// Unused.
	
	public void edgeAttributeAdded( String graphId, String edgeId, String attribute, Object value )
    {
    }

	public void edgeAttributeChanged( String graphId, String edgeId, String attribute,
            Object oldValue, Object newValue )
    {
    }

	public void edgeAttributeRemoved( String graphId, String edgeId, String attribute )
    {
    }

	public void nodeAttributeAdded( String graphId, String nodeId, String attribute, Object value )
    {
    }

	public void nodeAttributeChanged( String graphId, String nodeId, String attribute,
            Object oldValue, Object newValue )
    {
    }

	public void nodeAttributeRemoved( String graphId, String nodeId, String attribute )
    {
    }
}