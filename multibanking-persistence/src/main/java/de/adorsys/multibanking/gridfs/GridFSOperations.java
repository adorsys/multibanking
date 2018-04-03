package de.adorsys.multibanking.gridfs;

import com.mongodb.BasicDBObject;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSInputFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Mongo GridFS-Operations: Read, write, delete
 *
 * @author Christian Brandenstein
 */
@Component
public class GridFSOperations {

    private static final Logger LOG = LoggerFactory.getLogger(GridFSOperations.class);

    @Autowired
    private MongoTemplate mongoTemplate;

    public void store(String name, byte[] blob) {
        byte [] input = blob;

        // Store
        GridFS gridFS = new GridFS(mongoTemplate.getDb(), "booking");

        // Alte Datei löschen
        GridFSDBFile gridFSDBFile = gridFS.findOne(name);
        if (gridFSDBFile != null) {
            gridFS.remove(gridFSDBFile);
        }

        // Neue schreiben
        GridFSInputFile gfsFile = gridFS.createFile(input);
        gfsFile.setFilename(name);
        gfsFile.save();
    }

    public void delete(String filename) {
        GridFS gridFS = new GridFS(mongoTemplate.getDb(), "booking");

        GridFSDBFile gridFSDBFile = gridFS.findOne(filename);
        if (gridFSDBFile != null) {
            gridFS.remove(gridFSDBFile);
        }
    }

    public InputStream read(String filename) throws IOException {
        GridFS gridFS = new GridFS(mongoTemplate.getDb(), "booking");

        GridFSDBFile gridFSDBFile = gridFS.findOne(filename);
        if (gridFSDBFile != null) {
            return gridFSDBFile.getInputStream();
        }
        return null;
    }


    public List<String> filenames(String inhaberId) {
        GridFS gridFS = new GridFS(mongoTemplate.getDb(), "booking");
        BasicDBObject regex = new BasicDBObject("$regex", "^" + Pattern.quote(inhaberId));
        BasicDBObject whereQuery = new BasicDBObject("filename", regex);
        List<GridFSDBFile> files = gridFS.find(whereQuery);

        List<String> filenames = new ArrayList<>();
        files.forEach( f -> filenames.add(f.getFilename()));
        return filenames;
    }
}
