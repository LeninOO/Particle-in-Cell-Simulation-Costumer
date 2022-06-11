package ui.util.projection;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/** Scene of all objects. Sorts objects by distance
 * before painting. */
public class Scene {
	private ArrayList<AbstractObject> objectlist = new ArrayList<AbstractObject>();
	private ArrayList<Distance> distancelist = new ArrayList<Distance>();

	/** Timeout of drawing operation in nanoseconds */
	public long timeout = 100 * 1000 * 1000; // 100 ms

	public boolean drawtimeout = false;
	public long lastrendertime = 0;

	/** Add object to scene */
	public void add(AbstractObject object) {
		objectlist.add(object);
	}

	private void applyProjection(Projection projection) {
		for (AbstractObject o : objectlist) {
			o.applyProjection(projection);
		}
	}

	private void createObjectList() {
		distancelist.clear();
		for (AbstractObject o : objectlist) {
			for (PaintObject p : o.getObjects()) {
				Distance d = new Distance();
				d.distance = p.getDistance();
				d.object = p;
				distancelist.add(d);
			}
		}
	}

	private void sort() {
		Collections.sort(distancelist, new DistanceComparator());
	}

	/** Paint all objects in order of distance */
	public void paint(Projection projection, Graphics2D graphics) {
		applyProjection(projection);
		createObjectList();
		sort();
		long start = System.nanoTime();
		boolean t = false;
		for (Distance d : distancelist) {
			d.object.paint(projection, graphics);
			if (System.nanoTime() - start > timeout) {
				t = true;
				break;
			}
		}
		drawtimeout = t;
		lastrendertime = System.nanoTime() - start;
	}

	private class Distance {
		double distance;
		PaintObject object;
	}

	private class DistanceComparator implements Comparator<Distance> {
		@Override
		public int compare(Distance s1, Distance s2) {
			return Double.compare(s1.distance, s2.distance);
		}
	}
}
