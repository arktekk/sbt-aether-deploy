package org.apache.maven.repository.internal;

import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.deployment.DeployRequest;
import org.eclipse.aether.impl.MetadataGenerator;
import org.eclipse.aether.installation.InstallRequest;
import org.eclipse.aether.metadata.Metadata;

import java.util.*;

class OverrideVersionsMetadataGenerator
        implements MetadataGenerator {

    private Map<Object, VersionsMetadataDelegate> versions;

    private Map<Object, VersionsMetadataDelegate> processedVersions;

    OverrideVersionsMetadataGenerator(InstallRequest request) {
        this(request.getMetadata());
    }

    OverrideVersionsMetadataGenerator(DeployRequest request) {
        this(request.getMetadata());
    }

    private OverrideVersionsMetadataGenerator(Collection<? extends Metadata> metadatas) {
        versions = new LinkedHashMap<>();
        processedVersions = new LinkedHashMap<>();

        for (Iterator<? extends Metadata> it = metadatas.iterator(); it.hasNext(); ) {
            Metadata metadata = it.next();
            if (metadata instanceof VersionsMetadataDelegate) {
                it.remove();
                VersionsMetadataDelegate versionsMetadata = (VersionsMetadataDelegate) metadata;
                processedVersions.put(versionsMetadata.getKey(), versionsMetadata);
            }
        }
    }

    public Collection<? extends Metadata> prepare(Collection<? extends Artifact> artifacts) {
        return Collections.emptyList();
    }

    public Artifact transformArtifact(Artifact artifact) {
        return artifact;
    }

    public Collection<? extends Metadata> finish(Collection<? extends Artifact> artifacts) {
        for (Artifact artifact : artifacts) {
            Object key = VersionsMetadata.getKey(artifact);
            if (processedVersions.get(key) == null) {
                VersionsMetadataDelegate versionsMetadata = versions.get(key);
                if (versionsMetadata == null) {
                    versionsMetadata = new VersionsMetadataDelegate(new VersionsMetadata(artifact), artifact);
                    versions.put(key, versionsMetadata);
                }
            }
        }

        return versions.values();
    }

}
