package org.apache.maven.repository.internal;

import aether.MavenCoordinates;
import org.apache.maven.artifact.repository.metadata.Snapshot;
import org.apache.maven.artifact.repository.metadata.Versioning;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.impl.MetadataGenerator;
import org.eclipse.aether.installation.InstallRequest;
import org.eclipse.aether.metadata.Metadata;
import org.eclipse.aether.util.ConfigUtils;

import java.util.*;

class OverrideLocalSnapshotMetadataGenerator
        implements MetadataGenerator {

    private Map<Object, LocalSnapshotMetadata> snapshots;

    private final boolean legacyFormat;

    OverrideLocalSnapshotMetadataGenerator(RepositorySystemSession session, InstallRequest unused) {
        legacyFormat = ConfigUtils.getBoolean(session.getConfigProperties(), false, "maven.metadata.legacy");
        snapshots = new LinkedHashMap<>();
    }

    public Collection<? extends Metadata> prepare(Collection<? extends Artifact> artifacts) {
        for (Artifact artifact : artifacts) {
            if (artifact.isSnapshot()) {
                Object key = LocalSnapshotMetadata.getKey(artifact);
                LocalSnapshotMetadata snapshotMetadata = snapshots.get(key);
                if (snapshotMetadata == null) {
                    snapshotMetadata = new LocalSnapshotMetadata(createMetadata(artifact, legacyFormat), artifact.getFile(), legacyFormat, new Date());
                    snapshots.put(key, snapshotMetadata);
                }
                snapshotMetadata.bind(artifact);
            }
        }

        return Collections.emptyList();
    }

    private static org.apache.maven.artifact.repository.metadata.Metadata createMetadata(Artifact artifact, boolean legacyFormat) {
        //todo: Consider calling LocalSnapshotMetadata.createMetadata using reflection
        Snapshot snapshot = new Snapshot();
        snapshot.setLocalCopy(true);
        Versioning versioning = new Versioning();
        versioning.setSnapshot(snapshot);

        org.apache.maven.artifact.repository.metadata.Metadata metadata = new org.apache.maven.artifact.repository.metadata.Metadata();
        metadata.setVersioning(versioning);
        metadata.setGroupId(artifact.getGroupId());
        metadata.setVersion(artifact.getBaseVersion());
        if (!legacyFormat) {
            metadata.setModelVersion("1.1.0");
        }

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

    public Artifact transformArtifact(Artifact artifact) {
        return artifact;
    }

    public Collection<? extends Metadata> finish(Collection<? extends Artifact> artifacts) {
        return snapshots.values();
    }

}
