package org.apache.maven.repository.internal;

import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.deployment.DeployRequest;
import org.eclipse.aether.impl.MetadataGenerator;
import org.eclipse.aether.impl.MetadataGeneratorFactory;
import org.eclipse.aether.installation.InstallRequest;

public class OverrideSnapshotMetadataGeneratorFactory
        implements MetadataGeneratorFactory {

    public MetadataGenerator newInstance(RepositorySystemSession session, InstallRequest request) {
        // TODO: check that aetherInstall works properly with sbt plugin snapshots
        return new OverrideLocalSnapshotMetadataGenerator(session, request);
    }

    public MetadataGenerator newInstance(RepositorySystemSession session, DeployRequest request) {
        return new RemoteSnapshotMetadataGenerator(session, request);
    }

    public float getPriority() {
        return 10;
    }

}
