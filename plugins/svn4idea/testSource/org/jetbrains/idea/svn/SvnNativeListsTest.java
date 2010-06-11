package org.jetbrains.idea.svn;

import com.intellij.openapi.vcs.VcsConfiguration;
import com.intellij.openapi.vcs.changes.*;
import com.intellij.openapi.vfs.VirtualFile;
import org.junit.Test;
import org.tmatesoft.svn.core.SVNException;

import java.util.Collection;
import java.util.List;

public class SvnNativeListsTest extends SvnTestCase {
  private ChangeListManager myChangeListManager;
  private VcsDirtyScopeManager myDirtyScopeManager;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    myChangeListManager = ChangeListManager.getInstance(myProject);
    myDirtyScopeManager = VcsDirtyScopeManager.getInstance(myProject);
  }

  @Override
  public void tearDown() throws Exception {
    final List<LocalChangeList> changeListList = myChangeListManager.getChangeLists();
    for (LocalChangeList list : changeListList) {
      if (SvnChangeProvider.ourDefaultListName.equals(list.getName())) continue;
      final Collection<Change> changes = list.getChanges();
      for (Change change : changes) {
        clearListForRevision(change.getBeforeRevision());
        clearListForRevision(change.getAfterRevision());
      }
    }

    super.tearDown();
  }

  private void clearListForRevision(final ContentRevision revision) throws SVNException {
    if (revision == null) return;
    SvnChangelistListener.removeFromList(myProject, revision.getFile().getIOFile());
  }

  @Test
  public void testAdd() throws Throwable {
    final LocalChangeList newL = myChangeListManager.addChangeList("newOne", null);
    myDirtyScopeManager.markEverythingDirty();
    myChangeListManager.ensureUpToDate(false);

    myChangeListManager.setDefaultChangeList(newL);

    enableSilentOperation(VcsConfiguration.StandardConfirmation.ADD);
    final VirtualFile file = createFileInCommand("a.txt", "old content");

    //verify(runSvn("status"), "A a.txt");

    myDirtyScopeManager.markEverythingDirty();
    myChangeListManager.ensureUpToDate(false);

    verify(runSvn("status"), "", "--- Changelist 'newOne':", "A a.txt");
  }

  @Test
  public void testDeleted() throws Throwable {
    final LocalChangeList newL = myChangeListManager.addChangeList("newOne", null);
    myDirtyScopeManager.markEverythingDirty();
    myChangeListManager.ensureUpToDate(false);

    myChangeListManager.setDefaultChangeList(newL);

    enableSilentOperation(VcsConfiguration.StandardConfirmation.ADD);
    enableSilentOperation(VcsConfiguration.StandardConfirmation.REMOVE);
    final VirtualFile file = createFileInCommand("a.txt", "old content");
    checkin();
    deleteFileInCommand(file);

    //verify(runSvn("status"), "D a.txt");

    myDirtyScopeManager.markEverythingDirty();
    myChangeListManager.ensureUpToDate(false);

    verify(runSvn("status"), "", "--- Changelist 'newOne':", "D a.txt");
  }

  @Test
  public void testEdit() throws Throwable {
    final LocalChangeList newL = myChangeListManager.addChangeList("newOne", null);
    myDirtyScopeManager.markEverythingDirty();
    myChangeListManager.ensureUpToDate(false);

    myChangeListManager.setDefaultChangeList(newL);

    enableSilentOperation(VcsConfiguration.StandardConfirmation.ADD);
    final VirtualFile file = createFileInCommand("a.txt", "old content");
    checkin();
    editFileInCommand(myProject, file, "111");

    //verify(runSvn("status"), "M a.txt");

    myDirtyScopeManager.markEverythingDirty();
    myChangeListManager.ensureUpToDate(false);

    verify(runSvn("status"), "", "--- Changelist 'newOne':", "M a.txt");
  }

  @Test
  public void testEditAndMove() throws Throwable {
    final LocalChangeList newL = myChangeListManager.addChangeList("newOne", null);

    myDirtyScopeManager.markEverythingDirty();
    myChangeListManager.ensureUpToDate(false);

    myChangeListManager.setDefaultChangeList(newL);

    enableSilentOperation(VcsConfiguration.StandardConfirmation.ADD);
    enableSilentOperation(VcsConfiguration.StandardConfirmation.REMOVE);
    final VirtualFile file = createFileInCommand("a.txt", "old content");
    checkin();
    editFileInCommand(myProject, file, "111");

    //verify(runSvn("status"), "M a.txt");

    myDirtyScopeManager.markEverythingDirty();
    myChangeListManager.ensureUpToDate(false);

    verify(runSvn("status"), "", "--- Changelist 'newOne':", "M a.txt");

    renameFileInCommand(file, "b.txt");
    verify(runSvn("status"), "", "--- Changelist 'newOne':", "A + b.txt", "D a.txt");

    myDirtyScopeManager.markEverythingDirty();
    myChangeListManager.ensureUpToDate(false);

    verify(runSvn("status"), "", "--- Changelist 'newOne':", "A + b.txt", "D a.txt");
  }

  @Test
  public void testMove() throws Throwable {
    final LocalChangeList newL = myChangeListManager.addChangeList("newOne", null);
    myDirtyScopeManager.markEverythingDirty();
    myChangeListManager.ensureUpToDate(false);

    myChangeListManager.setDefaultChangeList(newL);

    enableSilentOperation(VcsConfiguration.StandardConfirmation.ADD);
    enableSilentOperation(VcsConfiguration.StandardConfirmation.REMOVE);
    final VirtualFile file = createFileInCommand("a.txt", "old content");
    checkin();

    renameFileInCommand(file, "b.txt");
    //verify(runSvn("status"), "A + b.txt", "D a.txt");

    myDirtyScopeManager.markEverythingDirty();
    myChangeListManager.ensureUpToDate(false);

    verify(runSvn("status"), "", "--- Changelist 'newOne':", "A + b.txt", "D a.txt");
  }

  @Test
  public void testMoveMove() throws Throwable {
    final LocalChangeList newL = myChangeListManager.addChangeList("newOne", null);
    myDirtyScopeManager.markEverythingDirty();
    myChangeListManager.ensureUpToDate(false);

    myChangeListManager.setDefaultChangeList(newL);

    enableSilentOperation(VcsConfiguration.StandardConfirmation.ADD);
    enableSilentOperation(VcsConfiguration.StandardConfirmation.REMOVE);
    final VirtualFile file = createFileInCommand("a.txt", "old content");
    checkin();

    renameFileInCommand(file, "b.txt");
    //verify(runSvn("status"), "A + b.txt", "D a.txt");

    myDirtyScopeManager.markEverythingDirty();
    myChangeListManager.ensureUpToDate(false);

    verify(runSvn("status"), "", "--- Changelist 'newOne':", "A + b.txt", "D a.txt");

    renameFileInCommand(file, "c.txt");
    verify(runSvn("status"), "", "--- Changelist 'newOne':", "A + c.txt", "D a.txt");

    myDirtyScopeManager.markEverythingDirty();
    myChangeListManager.ensureUpToDate(false);

    verify(runSvn("status"), "", "--- Changelist 'newOne':", "A + c.txt", "D a.txt");
  }
}
