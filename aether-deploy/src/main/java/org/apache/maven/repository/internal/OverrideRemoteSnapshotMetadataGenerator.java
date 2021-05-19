package org.apache.maven.repository.internal;

import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.deployment.DeployRequest;

public class OverrideRemoteSnapshotMetadataGenerator extends RemoteSnapshotMetadataGenerator {
    public OverrideRemoteSnapshotMetadataGenerator(RepositorySystemSession session, DeployRequest request) {
        super(session, request);
    }
}
