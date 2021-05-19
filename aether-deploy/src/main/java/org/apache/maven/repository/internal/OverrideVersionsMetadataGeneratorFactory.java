package org.apache.maven.repository.internal;

import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.deployment.DeployRequest;
import org.eclipse.aether.impl.MetadataGenerator;
import org.eclipse.aether.impl.MetadataGeneratorFactory;
import org.eclipse.aether.installation.InstallRequest;

public class OverrideVersionsMetadataGeneratorFactory
        implements MetadataGeneratorFactory {

    public MetadataGenerator newInstance(RepositorySystemSession session, InstallRequest request) {
        return new OverrideVersionsMetadataGenerator(session, request);
    }

    public MetadataGenerator newInstance(RepositorySystemSession session, DeployRequest request) {
        return new OverrideVersionsMetadataGenerator(session, request);
    }

    public float getPriority() {
        return 5;
    }
}
