package org.apache.maven.repository.internal;

import aether.MavenCoordinates;
import org.apache.maven.artifact.repository.metadata.Metadata;
import org.eclipse.aether.artifact.Artifact;

import java.io.File;

public class VersionsMetadataDelegate extends MavenMetadata {
    private final VersionsMetadata delegate;
    private final Artifact artifact;

    public VersionsMetadataDelegate(VersionsMetadata delegate, Artifact artifact) {
        this(delegate, artifact, artifact.getFile());
    }

    public VersionsMetadataDelegate(VersionsMetadata delegate, Artifact artifact, File file) {
        super(patchMetadata(delegate.metadata, artifact), file);
        this.delegate = delegate;
        this.artifact = artifact;
    }

    @Override
    protected void merge(Metadata metadata) {
        delegate.merge(metadata);
    }

    public MavenMetadata setFile(File file) {
        return new VersionsMetadataDelegate(delegate, artifact, file);
    }

    public Object getKey() {
        return delegate.getKey();
    }

    public String getGroupId() {
        return delegate.getGroupId();
    }

    public String getArtifactId() {
        return delegate.getArtifactId();
    }

    public String getVersion() {
        return delegate.getVersion();
    }

    public Nature getNature() {
        return delegate.getNature();
    }

    private static Metadata patchMetadata(Metadata metadata, Artifact artifact) {
        boolean sbtPlugin = Boolean.parseBoolean(artifact.getProperty(MavenCoordinates.SbtPlugin(), "false"));
        if (sbtPlugin) {
            String scalaVersion = artifact.getProperty(MavenCoordinates.ScalaVersion(), "");
            String sbtVersion = artifact.getProperty(MavenCoordinates.SbtVersion(), "");
            metadata.setArtifactId(artifact.getArtifactId() + "_" + scalaVersion + "_" + sbtVersion);
        } else {
            metadata.setArtifactId(artifact.getArtifactId());
        }
        return metadata;
    }
}
