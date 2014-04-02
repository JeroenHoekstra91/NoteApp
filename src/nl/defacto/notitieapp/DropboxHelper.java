package nl.defacto.notitieapp;

import java.io.IOException;
import java.util.List;

import android.app.Activity;
import android.content.Context;

import com.dropbox.sync.android.DbxAccountManager;
import com.dropbox.sync.android.DbxException;
import com.dropbox.sync.android.DbxException.Unauthorized;
import com.dropbox.sync.android.DbxFile;
import com.dropbox.sync.android.DbxFileInfo;
import com.dropbox.sync.android.DbxFileSystem;
import com.dropbox.sync.android.DbxPath;
import com.dropbox.sync.android.DbxPath.InvalidPathException;

public class DropboxHelper {
	static final int LINK_DB = 1;
	
	private DbxAccountManager mDbxAcctMgr;
	private Context context;
	private Activity activity;
	
	public DropboxHelper(Context context, Activity activity) throws Unauthorized {
		this.context = context;
		this.activity = activity;
		
		link();
	}
	
	private void link() throws Unauthorized {
		mDbxAcctMgr = DbxAccountManager.getInstance(context, "2cavkxlkgqtngx1", "sp1cy7i81pudjaw");
		
		if(!mDbxAcctMgr.hasLinkedAccount())
			mDbxAcctMgr.startLink(activity, LINK_DB);
	}
	
	public List<DbxFileInfo> fetchNotes() throws InvalidPathException, DbxException {
		DbxFileSystem dbxFs = DbxFileSystem.forAccount(mDbxAcctMgr.getLinkedAccount());
		return dbxFs.listFolder(new DbxPath(""));
	}
	
	public void saveNote(String title, String body) throws InvalidPathException, IOException {
		DbxFileSystem dbxFs = DbxFileSystem.forAccount(mDbxAcctMgr.getLinkedAccount());
		DbxFile testFile = dbxFs.create(new DbxPath(title));
		try {
		    testFile.writeString(body);
		} finally {
		    testFile.close();
		}
	}
	
	public String loadNote(String note) throws InvalidPathException, IOException {
		DbxFileSystem dbxFs = DbxFileSystem.forAccount(mDbxAcctMgr.getLinkedAccount());
		DbxFile testFile = dbxFs.open(new DbxPath(note));
		String content = null;
		try {
		    content = testFile.readString();
		} finally {
		    testFile.close();
		}
		
		return content;
	}
}
