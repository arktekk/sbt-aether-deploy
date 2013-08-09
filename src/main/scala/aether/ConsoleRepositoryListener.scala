package aether

import org.eclipse.aether.{RepositoryEvent, AbstractRepositoryListener}
import sbt.Logger

class ConsoleRepositoryListener(out: Logger) extends AbstractRepositoryListener {
  override def artifactDeployed(event: RepositoryEvent) {
    out.info("Deployed " + event.getArtifact + " to " + event.getRepository)
  }

  override def artifactDeploying(event: RepositoryEvent) {
    out.info("Deploying " + event.getArtifact + " to " + event.getRepository)
  }

  override def artifactDescriptorInvalid(event: RepositoryEvent) {
    out.info("Invalid artifact descriptor for " + event.getArtifact + ": "
      + event.getException.getMessage)
  }

  override def artifactDescriptorMissing(event: RepositoryEvent) {
    out.info("Missing artifact descriptor for " + event.getArtifact)
  }

  override def artifactDownloaded(event: RepositoryEvent) {
    out.info("Downloaded artifact " + event.getArtifact + " from " + event.getRepository)
  }

  override def artifactDownloading(event: RepositoryEvent) {
    out.info("Downloading artifact " + event.getArtifact + " from " + event.getRepository)
  }

  override def artifactInstalled(event: RepositoryEvent) {
    out.info("Installed " + event.getArtifact + " to " + event.getFile)
  }

  override def artifactInstalling(event: RepositoryEvent) {
    out.info("Installing " + event.getArtifact + " to " + event.getFile)
  }

  override def artifactResolved(event: RepositoryEvent) {
    out.info("Resolved artifact " + event.getArtifact + " from " + event.getRepository)
  }

  override def artifactResolving(event: RepositoryEvent) {
    out.info("Resolving artifact " + event.getArtifact)
  }

  override def metadataDeployed(event: RepositoryEvent) {
    out.info("Deployed " + event.getMetadata + " to " + event.getRepository)
  }

  override def metadataDeploying(event: RepositoryEvent) {
    out.info("Deploying " + event.getMetadata + " to " + event.getRepository)
  }

  override def metadataDownloaded(event: RepositoryEvent) {
    out.info("Downloaded metadata " + event.getMetadata + " from " + event.getRepository)
  }

  override def metadataDownloading(event: RepositoryEvent) {
    out.info("Downloading metadata " + event.getMetadata + " from " + event.getRepository)
  }

  override def metadataInstalled(event: RepositoryEvent) {
    out.info("Installed " + event.getMetadata + " to " + event.getFile)
  }

  override def metadataInstalling(event: RepositoryEvent) {
    out.info("Installing " + event.getMetadata + " to " + event.getFile)
  }

  override def metadataInvalid(event: RepositoryEvent) {
    out.info("Invalid metadata " + event.getMetadata)
  }

  override def metadataResolved(event: RepositoryEvent) {
    out.info("Resolved metadata " + event.getMetadata + " from " + event.getRepository)
  }

  override def metadataResolving(event: RepositoryEvent) {
    out.info("Resolving metadata " + event.getMetadata + " from " + event.getRepository)
  }
}