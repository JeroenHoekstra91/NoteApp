package nl.defacto.notitieapp;

import android.app.Activity;
import android.content.Context;

import com.dropbox.sync.android.DbxAccountManager;
import com.dropbox.sync.android.DbxFileSystem;

public class DropboxHelper {
	static final int LINK_DB = 1;
	
	private DbxAccountManager mDbxAcctMgr;
	private DbxFileSystem dbxFs;
	
	public DropboxHelper(Context context, Activity activity) {
		mDbxAcctMgr = DbxAccountManager.getInstance(context, "2cavkxlkgqtngx1", "sp1cy7i81pudjaw");
		
		if(!mDbxAcctMgr.hasLinkedAccount())
			mDbxAcctMgr.startLink(activity, LINK_DB);
	}
}
