package org.apache.maven.repository.internal;

import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.deployment.DeployRequest;
import org.eclipse.aether.impl.MetadataGenerator;
import org.eclipse.aether.impl.MetadataGeneratorFactory;
import org.eclipse.aether.installation.InstallRequest;

public class OverrideVersionsMetadataGeneratorFactory
        implements MetadataGeneratorFactory {

    public MetadataGenerator newInstance(RepositorySystemSession unused, InstallRequest request) {
        return new OverrideVersionsMetadataGenerator(request);
    }

    public MetadataGenerator newInstance(RepositorySystemSession unused, DeployRequest request) {
        return new OverrideVersionsMetadataGenerator(request);
    }

    public float getPriority() {
        return 5;
    }
}
