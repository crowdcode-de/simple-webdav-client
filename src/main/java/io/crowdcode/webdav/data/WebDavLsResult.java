package io.crowdcode.webdav.data;

import java.util.ArrayList;
import java.util.List;

/**
 * Result of a webdav list command
 */
public class WebDavLsResult {

    private final List<WebDavFile> files = new ArrayList<>();
    private final List<WebDavDirectory> directories = new ArrayList<>();

    public void addFile(WebDavFile file) {
        files.add(file);
    }

    public void addDirectory(WebDavDirectory dir) {
        directories.add(dir);
    }

    public List<WebDavElement> getAllSubElements() {
        ArrayList<WebDavElement> objects = new ArrayList<>();
        objects.addAll(files);
        objects.addAll(directories);
        return objects;
    }

    public List<WebDavFile> getFiles() {
        return files;
    }

    public List<WebDavDirectory> getDirectories() {
        return directories;
    }
}
