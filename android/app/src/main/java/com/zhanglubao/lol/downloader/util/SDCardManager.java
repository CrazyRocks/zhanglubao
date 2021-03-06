/*
 * Copyright © 2012-2013 LiuZhongnan. All rights reserved.
 * 
 * Email:qq81595157@126.com
 * 
 * PROPRIETARY/CONFIDENTIAL.
 */

package com.zhanglubao.lol.downloader.util;

import android.annotation.SuppressLint;
import android.os.Environment;
import android.os.StatFs;

import com.zhanglubao.lol.config.ZLBConfiguration;
import com.zhanglubao.lol.util.UIUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;


/**
 * SDCardManager.SD卡管理，空间数据的获取
 * 
 * @author Rocks 451360994@qq.com
 * @version v3.5
 * @created time 2012-11-5 下午1:16:02
 */
public class SDCardManager {

	private String sdPath;
	private long nSDTotalSize;
	private long nSDFreeSize;

	public SDCardManager(String sdPath) {
		this.sdPath = sdPath;
		init();
	}

	@SuppressWarnings("deprecation")
	private void init() {
		try {
			StatFs statFs = new StatFs(sdPath);
			long totalBlocks = statFs.getBlockCount();// 区域块数
			long availableBlocks = statFs.getAvailableBlocks();// 可利用区域块数
			long blockSize = statFs.getBlockSize();// 每个区域块大小
			nSDTotalSize = totalBlocks * blockSize;
			nSDFreeSize = availableBlocks * blockSize;
		} catch (Exception e) {

		}
	}

	/**
	 * 是否存在
	 * 
	 * @return
	 */
	public boolean exist() {
		return nSDTotalSize == 0 ? false : true;
	}

	/**
	 * 总空间
	 * 
	 * @return
	 */
	public long getTotalSize() {
		return nSDTotalSize;
	}

	/**
	 * 剩余空间
	 * 
	 * @return
	 */
	public long getFreeSize() {
		return nSDFreeSize;
	}

	/**
	 * 优酷视频占有空间大小(/zhanglubao/offlinedata/下的文件大小)
	 * 
	 * @return
	 */
	public long getZlbVideoSpace() {
		try {
			if (sdPath.equals(getDefauleSDCardPath())) {
				// 固定加入p2p所占空间大小100M
//				if (AcceleraterServiceManager.isAccSupported(Zlb.context)) {
//					return getZlbOfflinedataSpace() + 100 * 1024 * 1024;
//				} else {
					return getZlbOfflinedataSpace();
//				}
			} else {
				return getZlbOfflinedataSpace();
			}
		} catch (Exception e) {
			return getZlbOfflinedataSpace();
		}
	}

	private long getZlbOfflinedataSpace() {
		File f = new File(sdPath + ConfigUtil.getDownloadPath());
		return f.exists() ? getFileSize(f) : 0;
	}

	private long getZlbdiskSpace() {
		File f = new File(sdPath + ConfigUtil.DISK_PATH);
		return f.exists() ? getFileSize(f) : 0;
	}

	/**
	 * 其他程序占有空间大小
	 * 
	 * @return
	 */
	public long getOtherSpace() {
		if (!exist())
			return 0;
		return nSDTotalSize - nSDFreeSize - getZlbOfflinedataSpace()
				- getZlbdiskSpace();
	}

	/**
	 * 优酷视频所占空间比例n%
	 */
	public int getZlbProgrss() {
		if (!exist())
			return 0;
		return (int) ((1000 * getZlbVideoSpace()) / nSDTotalSize);
	}

	/**
	 * 其他程序所占空间比例n%
	 */
	public int getOtherProgrss() {
		if (!exist())
			return 0;
		return (int) ((1000 * getOtherSpace()) / nSDTotalSize);
	}

	/**
	 * TODO 递归取得文件夹大小
	 */
	private static long getFileSize(File f) {
		long size = 0;
		if (f.isDirectory()) {
			File files[] = f.listFiles();
			if (files != null) {
				for (int i = 0, n = files.length; i < n; i++) {
					if (files[i].isDirectory()) {
						size = size + getFileSize(files[i]);
					} else {
						size = size + files[i].length();
					}
				}
			}
		} else {
			size = f.length();
		}
		return size;
	}

	/** Returns 是否有SD卡 */
	public static boolean hasSDCard() {
		return Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED);
	}

	public static String getDefauleSDCardPath() {
		return hasSDCard() ? Environment.getExternalStorageDirectory()
				.getAbsolutePath() : "";
	}

	/**
	 * 获得外部存储路径
	 * 
	 * @return /mnt/sdcard或者/storage/extSdCard等多种名称
	 */
	@SuppressLint("NewApi")
	public static ArrayList<SDCardInfo> getExternalStorageDirectory() {
		ArrayList<SDCardInfo> list = new ArrayList<SDCardInfo>();
		if (UIUtils.hasKitKat()) {
			// OS 4.4 以上读取外置 SD 卡
			final File[] externalFiles = ZLBConfiguration.context
					.getExternalFilesDirs(null);
			if (null != externalFiles) {
				SDCardInfo info = new SDCardInfo();
				info.path = getDefauleSDCardPath();
				info.isExternal = false;
				list.add(info);
				if (externalFiles.length > 1 && (null != externalFiles[1])) {
					SDCardInfo externalInfo = new SDCardInfo();
					externalInfo.path = externalFiles[1].getAbsolutePath();
					externalInfo.isExternal = true;
					list.add(externalInfo);
				}
			}
			return list;
		} else {
			Runtime runtime = Runtime.getRuntime();
			Process proc;
			try {
				proc = runtime.exec("mount");
				// InputStream is = Zlb.context.getAssets().open("mount.txt");
				InputStream is = proc.getInputStream();
				InputStreamReader isr = new InputStreamReader(is);
				String line;
				// String mount = new String();
				BufferedReader br = new BufferedReader(isr);
				String defauleSDCardPath = getDefauleSDCardPath();
				//用于盛放已经判断过的路径，省时高效
				HashMap<String,Integer> tempPath = new HashMap<String,Integer>();
				while ((line = br.readLine()) != null) {
//					 Logger.d("nathan2", line);
					// "fat"为不可卸载的；"fuse"为可卸载的；但是有的手机不适用，所以统一去处理;storage为一些三星手机的存储路径标识
					if (line.contains("fat") || line.contains("fuse")
							|| line.contains("storage")) {
						if (line.contains("secure") || line.contains("asec")
								|| line.contains("firmware")
								|| line.contains("shell")
								|| line.contains("obb")
								|| line.contains("legacy")
								|| line.contains("data")
								|| line.contains("tmpfs")) {

							continue;
						}
						String columns[] = line.split(" ");
						for (int i = 0; i < columns.length; i++) {
							String path = columns[i];
							//一加手机的"/dev/fuse"是假地址
							if (path.contains("/") && !path.contains("data") && !path.contains("Data")
									&& !path.contains("/dev/fuse")) {
								try {
									if (tempPath.containsKey(path)) {
										continue;
									} else {
										tempPath.put(path, 0);
										SDCardManager m = new SDCardManager(path);
										if (m.getTotalSize() >= 1024 * 1024 * 1024) {
											SDCardInfo info = new SDCardInfo();
											info.path = columns[i];
											if (info.path.equals(defauleSDCardPath)) {
												info.isExternal = false;
											} else {
												info.isExternal = true;
											}
											list.add(info);
										}
									}
								} catch (Exception e) {
									continue;
								}
							}
						}
					}
				}
				tempPath.clear();
				if (list.size() == 1) {
					if (!defauleSDCardPath.equals(list.get(0).path)) {
						SDCardInfo info = new SDCardInfo();
						info.path = defauleSDCardPath;
						info.isExternal = false;
						list.add(info);
					} else {
						PreferenceUtil.savePreference("download_file_path",
								defauleSDCardPath);
					}
				} else if (list.size() == 0) {
					if (defauleSDCardPath != null
							&& defauleSDCardPath.length() != 0) {
						SDCardInfo info = new SDCardInfo();
						info.path = defauleSDCardPath;
						info.isExternal = false;
						list.add(info);
					}
				}
				if (list.size() > 1) {
					Set<SDCardInfo> s = new TreeSet<SDCardInfo>(
							new Comparator<SDCardInfo>() {

								@Override
								public int compare(SDCardInfo o1, SDCardInfo o2) {
									return o1.path.compareTo(o2.path);
								}

							});
					s.addAll(list);
					list = new ArrayList<SDCardInfo>(s);
				}
				return list;
			} catch (IOException e) {

			}
			return null;

		}
	}

	public static class SDCardInfo {

		/** 路径/mnt/sdcard或者/storage/extSdCard等多种名称 */
		public String path;

		/** 是否是外部存储 */
		public boolean isExternal;
	}
}
