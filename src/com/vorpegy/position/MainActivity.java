package com.vorpegy.position;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.Region;

import android.app.Activity;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;

import com.vorpegy.position.LocationApplication.onRangeBeaconsInRegionListener;

public class MainActivity extends Activity implements
		onRangeBeaconsInRegionListener {
	private MyView mv;
	private ImageView img;
	// a, b, c, d�ֱ�Ϊ��ǰλ�õ��ĸ�ibeacon�ľ���
	private int a, b, c, d;
	// pa, pb, pc, pdΪ�����ĸ�������� pnΪ��ǰ��λ����
	private Point pa, pb, pc, pd, pn;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mv = new MyView(this);
		setContentView(mv);
		mv.init();
		hd.postDelayed(r, 2000);
		init();
	}

	private void init() {
		img = new ImageView(this);
		img.setBackgroundResource(R.drawable.a);
		pa = new Point(100, 50);
		pb = new Point(50, 100);
		pc = new Point(0, 50);
		pd = new Point(50, 0);
		pn = new Point(0, 0);
	}

	Handler hd = new Handler();
	Runnable r = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			mv.removeMarker(img);
			if (a != 0 && b != 0 && c != 0 && d != 0)
				pn = calculate(a, b, c, d);
			mv.addMarker(img, pn.x, pn.y, -0.5f, -0.5f);
			hd.postDelayed(this, 1000);
		}
	};

	@Override
	public void onRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
		// TODO Auto-generated method stub
		if (beacons != null) {
			for (Beacon beacon : beacons) {
				int minor = beacon.getId3().toInt();
				if (minor == 1) {
					a = calculateAccuracy(beacon.getRssi());
				}
				if (minor == 2) {
					b = calculateAccuracy(beacon.getRssi());
				}
				if (minor == 3) {
					c = calculateAccuracy(beacon.getRssi());
				}
				if (minor == 4) {
					d = calculateAccuracy(beacon.getRssi());
				}
			}

		}
	}

	@Override
	protected void onResume() {

		super.onResume();
		((LocationApplication) this.getApplication())
				.setOnRangeBeaconsInRegionListener(this);

	}

	@Override
	protected void onPause() {
		super.onPause();
		((LocationApplication) this.getApplication())
				.setOnRangeBeaconsInRegionListener(null);

	}

	/**
	 * 
	 * @param rssi
	 *            ����������ģ��
	 * @return
	 */
	private int calculateAccuracy(float rssi) {
		int txPower = -58;
		if (rssi == 0) {
			return (int) -1.0; // if we cannot determine accuracy, return -1.
		}
		double ratio = rssi * 1.0 / txPower;
		if (ratio < 1.0) {
			return (int) (Math.pow(ratio, 10) * 10);
		} else {
			return (int) (((0.89976) * Math.pow(ratio, 7.7095) + 0.111) * 10);

		}
	}

	/**
	 * ���ڼ���
	 * 
	 * @param distance1Ϊ����֪��ľ���
	 * @param distance2Ϊ����֪��ľ���
	 * @param p1x��֪��1��x����
	 * @param p1y��֪��1��y����
	 * @param p2x��֪��2��x����
	 * @param p2y��֪��2��y����
	 *            a,b,cΪ��ʽy=ax^+bx+c��ϵ��
	 * @return
	 */
	private ArrayList<Integer> getpoint(int distance1, int distance2, int p1x,
			int p1y, int p2x, int p2y) {
		double k, kb;
		double a, b, c;
		double x1, y1, x2, y2;
		// list���ڱ������������
		ArrayList<Integer> list = new ArrayList<Integer>();
		// lΪ������֪����������ֵ ���ں�����õ����� Ϊ��ֹ���� �����ֵΪ0���Ϊ1
		int l = p2y - p1y;
		if (l == 0)
			l = 1;
		k = (p1x - p2x) / l;
		kb = (distance1 * distance1 - distance2 * distance2 + p2x * p2x + p2y
				* p2y - p1x * p1x - p1y * p1y)
				/ (2 * l);
		a = 1 + k * k;
		b = 2 * k * (kb - p1y) - 2 * p1x;
		c = p1x * p1x + (kb - p1y) * (kb - p1y) - distance1 * distance1;
		// b^-4ac�жϸ��ĸ��� С��0�򷵻ؿ�list
		if ((b * b - 4 * a * c) < 0) {
			return list;
		}
		// x1,x2,y1,y2Ϊ���ص������
		x1 = (-b + Math.sqrt(b * b - 4 * a * c)) / (2 * a);
		x2 = (-b - Math.sqrt(b * b - 4 * a * c)) / (a * a);
		y1 = k * x1 + kb;
		y2 = k * x2 + kb;
		list.add((int) x1);
		list.add((int) y1);
		list.add((int) x2);
		list.add((int) y2);
		return list;
	}

	/**
	 * �˷������ؾ������Ƕ�λ���������� pΪ�쳣ʱ���ص����� p=��0��0��
	 * 
	 * @param a
	 *            =�����ibeacon1�ľ���
	 * @param b
	 *            =�����ibeacon2�ľ���
	 * @param c
	 *            =�����ibeacon3�ľ���
	 * @param d
	 *            =�����ibeacon4�ľ���
	 * @return
	 */
	private Point calculate(int a, int b, int c, int d) {
		Point p = new Point(0, 0);
		// list1,list2��list3��list4���ڱ�����������ֵ
		ArrayList<Integer> list1 = new ArrayList<Integer>();
		ArrayList<Integer> list2 = new ArrayList<Integer>();
		ArrayList<Integer> list3 = new ArrayList<Integer>();
		ArrayList<Integer> list4 = new ArrayList<Integer>();
		// listpoint���ڱ���4�������ĵ����������ֵ
		ArrayList<List<Integer>> listpoint = new ArrayList<List<Integer>>();
		// prox1,prox2,prox3,prox4���ڱ�ʾ�����ĵ��������
		// ����������������prox1,proy1,prox2,proy2�����Ĵ�
		int prox1, proy1, prox2, proy2;
		// p1,p2,p3,p4���ڱ����ĸ������ĵ����������
		Point p1 = new Point();
		Point p2 = new Point();
		Point p3 = new Point();
		Point p4 = new Point();
		// p5,p6Ϊ������������ �����ı��οɷֽ������������
		// ���������������� p5��p6����һ��
		Point p5 = new Point();
		Point p6 = new Point();
		// p7��p8Ϊp5��p6�����ĵ����ĵ�
		Point p7 = new Point();
		Point p8 = new Point();
		// p9Ϊ���շ��ص�����
		Point p9 = new Point();
		// ��������������ֵ
		// a��b,c,dΪλ�õ㵽��֪��pa,pb,pc,pd�ľ���
		// ֻ�������ڵ������� ��Ե��������������Ĵ��ڻ�����޽�����
		list1 = getpoint(a, b, pa.x, pa.y, pb.x, pb.y);
		list2 = getpoint(a, d, pa.x, pa.y, pd.x, pd.y);
		list3 = getpoint(b, c, pb.x, pb.y, pc.x, pc.y);
		list4 = getpoint(c, d, pc.x, pc.y, pd.x, pd.y);
		// ֮�����жϴ�С����Ϊ�˴������쳣
		if (list1.size() == 4)
			listpoint.add(list1);
		if (list2.size() == 4)
			listpoint.add(list2);
		if (list3.size() == 4)
			listpoint.add(list3);
		if (list4.size() == 4)
			listpoint.add(list4);
		if (listpoint.size() < 3)
			return p;
		// ������ĸ��� ���ĵ�����ı��� �ٷָ������������ �ֱ�������������ε�����
		// ȡ�������ĵ��е�Ϊ���ն�λ����
		if (listpoint.size() == 4) {
			prox1 = list1.get(0) - 50;
			proy1 = list1.get(1) - 50;
			prox2 = list1.get(2) - 50;
			proy2 = list1.get(3) - 50;
			// �������빫ʽ
			if (Math.sqrt(prox1 * prox1 + proy1 * proy1) > Math.sqrt(prox2
					* prox2 + proy2 * proy2)) {
				p1.x = list1.get(2);
				p1.y = list1.get(3);
			} else {
				p1.x = list1.get(0);
				p1.y = list1.get(1);
			}
			prox1 = list2.get(0) - 50;
			proy1 = list2.get(1) - 50;
			prox2 = list2.get(2) - 50;
			proy2 = list2.get(3) - 50;
			if (Math.sqrt(prox1 * prox1 + proy1 * proy1) > Math.sqrt(prox2
					* prox2 + proy2 * proy2)) {
				p2.x = list2.get(2);
				p2.y = list2.get(3);
			} else {
				p2.x = list2.get(0);
				p2.y = list2.get(1);
			}
			prox1 = list3.get(0) - 50;
			proy1 = list3.get(1) - 50;
			prox2 = list3.get(2) - 50;
			proy2 = list3.get(3) - 50;
			if (Math.sqrt(prox1 * prox1 + proy1 * proy1) > Math.sqrt(prox2
					* prox2 + proy2 * proy2)) {
				p3.x = list3.get(2);
				p3.y = list3.get(3);
			} else {
				p3.x = list3.get(0);
				p3.y = list3.get(1);
			}
			prox1 = list4.get(0) - 50;
			proy1 = list4.get(1) - 50;
			prox2 = list4.get(2) - 50;
			proy2 = list4.get(3) - 50;
			if (Math.sqrt(prox1 * prox1 + proy1 * proy1) > Math.sqrt(prox2
					* prox2 + proy2 * proy2)) {
				p4.x = list4.get(2);
				p4.y = list4.get(3);
			} else {
				p4.x = list4.get(0);
				p4.y = list4.get(1);
			}

			p5.x = (p1.x + p2.x + p3.x) / 3;
			p5.y = (p1.y + p2.y + p3.y) / 3;
			p6.x = (p1.x + p3.x + p4.x) / 3;
			p6.y = (p1.y + p3.y + p4.y) / 3;
			p7.x = (p5.x + p6.x) / 2;
			p7.y = (p5.y + p6.y) / 2;
			// System.out.println(p7);
			p5.x = (p1.x + p2.x + p4.x) / 3;
			p5.y = (p1.y + p2.y + p4.y) / 3;
			p6.x = (p2.x + p3.x + p4.x) / 3;
			p6.y = (p2.y + p3.y + p4.y) / 3;
			p8.x = (p5.x + p6.x) / 2;
			p8.y = (p5.y + p6.y) / 2;
			// p9ȡ�����ĵ����ĵ�����ĵ�
			// * 10.8��* 17.16 + 34.32����ת�����ֻ���Ļ����
			p9.x = (int) (((p7.x + p8.x) / 2) * 10.8);
			p9.y = 1716 - (int) (((p7.y + p8.y) / 2) * 17.16);
		}
		// System.out.println(p7);
		// System.out.println(getpoint(a, b, pa.x, pa.y, pb.x, pb.y) +
		// "-------->"
		// + p1);
		// System.out.println(getpoint(a, d, pa.x, pa.y, pd.x, pd.y) +
		// "-------->"
		// + p2);
		// System.out.println(getpoint(b, c, pb.x, pb.y, pc.x, pc.y) +
		// "-------->"
		// + p3);
		// System.out.println(getpoint(c, d, pc.x, pc.y, pd.x, pd.y) +
		// "-------->"
		// + p4);
		d(p9);
		// ���ܳ�����ͼ���������
		if (p9.x > 1080 || p9.x < 1 || p9.y > 1716 || p9.y < 1)
			p9 = p;
		return p9;
	}

	private void d(Object obj) {
		Log.d("MainActivity", String.valueOf(obj));
	}

}
