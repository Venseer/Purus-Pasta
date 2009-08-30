/*
 *  This file is part of the Haven & Hearth game client.
 *  Copyright (C) 2009 Fredrik Tolf <fredrik@dolda2000.com>, and
 *                     Björn Johannessen <johannessen.bjorn@gmail.com>
 *
 *  Redistribution and/or modification of this file is subject to the
 *  terms of the GNU Lesser General Public License, version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  Other parts of this source tree adhere to other copying
 *  rights. Please see the file `COPYING' in the root directory of the
 *  source tree for details.
 *
 *  A copy the GNU Lesser General Public License is distributed along
 *  with the source tree of which this file is a part in the file
 *  `doc/LPGL-3'. If it is missing for any reason, please see the Free
 *  Software Foundation's website at <http://www.fsf.org/>, or write
 *  to the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 *  Boston, MA 02111-1307 USA
 */

package haven;

import java.util.*;
import java.lang.ref.WeakReference;
import javax.media.opengl.*;
import javax.media.opengl.glu.GLU;

public abstract class TexRT extends TexGL {
    private static Map<GL, Collection<TexRT>> current = new WeakHashMap<GL, Collection<TexRT>>();
    private boolean inited = false;
    public Profile prof = new Profile(300);
    private Profile.Frame curf;
	
    public TexRT(Coord sz) {
	super(sz);
    }
	
    public void dispose() {
	Collection<TexRT> tc;
	synchronized(current) {
	    tc = current.get(mygl);
	}
	if(tc != null) {
	    synchronized(tc) {
		tc.remove(this);
	    }
	}
	super.dispose();
    }
	
    protected abstract void subrend(GOut g);
	
    protected void fill(GOut g) {
	GL gl = g.gl;
	Collection<TexRT> tc;
	synchronized(current) {
	    tc = current.get(gl);
	    if(tc == null) {
		tc = new HashSet<TexRT>();
		current.put(gl, tc);
	    }
	}
	synchronized(tc) {
	    tc.add(this);
	}
	inited = false;
    }
	
    private void subrend2(GOut g) {
	if(id < 0)
	    return;
	GL gl = g.gl;
	if(Config.profile)
	    curf = prof.new Frame();
	subrend(g);
	if(curf != null)
	    curf.tick("render");
	g.texsel(id);
	GOut.checkerr(gl);
	if(!inited) {
	    gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL.GL_RGBA, tdim.x, tdim.y, 0, GL.GL_RGBA, GL.GL_UNSIGNED_BYTE, null);
	    GOut.checkerr(gl);
	    inited = true;
	}
	gl.glCopyTexSubImage2D(GL.GL_TEXTURE_2D, 0, 0, 0, 0, 0, dim.x, dim.y);
	GOut.checkerr(gl);
	if(curf != null) {
	    curf.tick("copy");
	    curf.fin();
	    curf = null;
	}
    }
    
    public static void renderall(GOut g) {
	GL gl = g.gl;
	Collection<TexRT> tc;
	synchronized(current) {
	    tc = current.get(gl);
	}
	if(tc != null) {
	    synchronized(tc) {
		for(TexRT t : tc)
		    t.subrend2(g);
	    }
	}
    }
}
