package org.nakedobjects.viewer.skylark.core;

import org.nakedobjects.viewer.skylark.Bounds;
import org.nakedobjects.viewer.skylark.Canvas;
import org.nakedobjects.viewer.skylark.Click;
import org.nakedobjects.viewer.skylark.ContentDrag;
import org.nakedobjects.viewer.skylark.InternalDrag;
import org.nakedobjects.viewer.skylark.Location;
import org.nakedobjects.viewer.skylark.Padding;
import org.nakedobjects.viewer.skylark.Size;
import org.nakedobjects.viewer.skylark.View;
import org.nakedobjects.viewer.skylark.ViewAreaType;
import org.nakedobjects.viewer.skylark.ViewDrag;

import org.apache.log4j.Logger;


public class AbstractBorder extends AbstractViewDecorator {
    private static final Logger LOG = Logger.getLogger(AbstractBorder.class);
    protected int bottom;
    protected int left;
    private boolean onBorder;
    protected int right;
    protected int top;

    protected AbstractBorder(View view) {
        super(view);
    }

    protected Bounds contentArea() {
        return new Bounds(0, 0, getSize().getWidth() - right, getSize().getHeight() - bottom);
    }

    public void drag(InternalDrag drag) {
        drag.move(-getLeft(), -getTop());
        super.drag(drag);
    }

    public void dragCancel(InternalDrag drag) {
        drag.move(-getLeft(), -getTop());
        super.dragCancel(drag);
    }

    public View dragFrom(InternalDrag drag) {
        drag.move(-getLeft(), -getTop());

        return super.dragFrom(drag);
    }

    public void dragIn(ContentDrag drag) {
        drag.move(-getLeft(), -getTop());
        super.dragIn(drag);
    }

    public void dragOut(ContentDrag drag) {
        drag.move(-getLeft(), -getTop());
        super.dragOut(drag);
    }

    public void dragTo(InternalDrag drag) {
        drag.move(-getLeft(), -getTop());
        super.dragTo(drag);
    }

    public void draw(Canvas canvas) {
        canvas.reduce(getLeft(), getTop(), getRight(), getBottom());
        wrappedView.draw(canvas);
    }

    public void drop(ContentDrag drag) {
        drag.move(-getLeft(), -getTop());
        super.drop(drag);
    }

    public void drop(ViewDrag drag) {
        drag.move(-getLeft(), -getTop());
        super.drop(drag);
    }

    public void firstClick(Click click) {
        click.move(-getLeft(), -getTop());
        wrappedView.firstClick(click);
    }

    public int getBaseline() {
        return wrappedView.getBaseline() + getTop();
    }

    protected int getBottom() {
        return bottom;
    }

    protected int getLeft() {
        return left;
    }

    public Padding getPadding() {
        Padding padding = wrappedView.getPadding();
        padding.extendTop(getTop());
        padding.extendLeft(getLeft());
        padding.extendBottom(getBottom());
        padding.extendRight(getRight());

        return padding;
    }

    public Size getRequiredSize() {
        Size size = wrappedView.getRequiredSize();
        size.extend(getLeft() + getRight(), getTop() + getBottom());

        return size;
    }

    protected int getRight() {
        return right;
    }

    public Size getSize() {
        Size size = wrappedView.getSize();
        size.extend(getLeft() + getRight(), getTop() + getBottom());

        return size;
    }

    protected int getTop() {
        return top;
    }

    protected boolean overBorder(Location mouseLocation) {
        return !contentArea().contains(mouseLocation);
    }

    protected boolean overContent(Location mouseLocation) {
        return contentArea().contains(mouseLocation);
    }

   public View identify(Location location) {
        location.move(-getLeft(), -getTop());
        return wrappedView.identify(location);
    }
    
    protected boolean isOnBorder() {
        return onBorder;
    }

    public void mouseMoved(Location at) {
        boolean on = overBorder(at);
        if (onBorder != on) {
            markDamaged();
            onBorder = on;
            LOG.debug("On border " + onBorder);
        }

        at.move(-getLeft(), -getTop());
        wrappedView.mouseMoved(at);
    }
    
    public void exited() {
        onBorder = false;
        super.exited();
    }

    public View pickup(ContentDrag drag) {
        drag.move(-getLeft(), -getTop());

        return super.pickup(drag);
    }

    public View pickup(ViewDrag drag) {
        drag.move(-getLeft(), -getTop());

        return super.pickup(drag);
    }

    public void secondClick(Click click) {
        click.move(-getLeft(), -getTop());
        wrappedView.secondClick(click);
    }
    
	public void setRequiredSize(Size size) {
        Size wrappedSize = new Size(size);
        wrappedSize.extend(-getLeft() - getRight(), -getTop() - getBottom());
        wrappedView.setRequiredSize(wrappedSize);
	}
	
    public void setSize(Size size) {
        Size wrappedViewSize = new Size(size);
        wrappedViewSize.extend(-getLeft() - getRight(), -getTop() - getBottom());
        wrappedView.setSize(wrappedViewSize);
    }

    public void thirdClick(Click click) {
        click.move(-getLeft(), -getTop());
        wrappedView.thirdClick(click);
    }

    public ViewAreaType viewAreaType(Location mouseLocation) {
        Size size = wrappedView.getSize();
        Bounds bounds = new Bounds(getLeft(), getTop(), size.getWidth(), size.getHeight());

        if (bounds.contains(mouseLocation)) {
            mouseLocation.move(-getLeft(), -getTop());

            return wrappedView.viewAreaType(mouseLocation);
        } else {
            return ViewAreaType.VIEW;
        }
    }

}

/*
 * Naked Objects - a framework that exposes behaviourally complete business
 * objects directly to the user. Copyright (C) 2000 - 2004 Naked Objects Group
 * Ltd
 * 
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
 * Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * The authors can be contacted via www.nakedobjects.org (the registered address
 * of Naked Objects Group is Kingsway House, 123 Goldworth Road, Woking GU21
 * 1NR, UK).
 */
