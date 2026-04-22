package aether
package internal

import org.eclipse.aether.{AbstractRepositoryListener, RepositoryEvent}
import sbt.Logger

class ConsoleRepositoryListener(logger: Logger) extends AbstractRepositoryListener {
  override def artifactDeployed(event: RepositoryEvent): Unit = {
    logger.info("Deployed " + event.getArtifact + " to " + event.getRepository)
  }

  override def artifactDeploying(event: RepositoryEvent): Unit = {
    logger.info("Deploying " + event.getArtifact + " to " + event.getRepository)
  }

  override def artifactDescriptorInvalid(event: RepositoryEvent): Unit = {
    logger.info(
      "Invalid artifact descriptor for " + event.getArtifact + ": "
        + event.getException.getMessage
    )
  }

  override def artifactDescriptorMissing(event: RepositoryEvent): Unit = {
    logger.info("Missing artifact descriptor for " + event.getArtifact)
  }

  override def artifactDownloaded(event: RepositoryEvent): Unit = {
    logger.info("Downloaded artifact " + event.getArtifact + " from " + event.getRepository)
  }

  override def artifactDownloading(event: RepositoryEvent): Unit = {
    logger.info("Downloading artifact " + event.getArtifact + " from " + event.getRepository)
  }

  override def artifactInstalled(event: RepositoryEvent): Unit = {
    logger.info("Installed " + event.getArtifact + " to " + event.getFile)
  }

  override def artifactInstalling(event: RepositoryEvent): Unit = {
    logger.info("Installing " + event.getArtifact + " to " + event.getFile)
  }

  override def artifactResolved(event: RepositoryEvent): Unit = {
    logger.info("Resolved artifact " + event.getArtifact + " from " + event.getRepository)
  }

  override def artifactResolving(event: RepositoryEvent): Unit = {
    logger.info("Resolving artifact " + event.getArtifact)
  }

  override def metadataDeployed(event: RepositoryEvent): Unit = {
    logger.info("Deployed " + event.getMetadata + " to " + event.getRepository)
  }

  override def metadataDeploying(event: RepositoryEvent): Unit = {
    logger.info("Deploying " + event.getMetadata + " to " + event.getRepository)
  }

  override def metadataDownloaded(event: RepositoryEvent): Unit = {
    logger.info("Downloaded metadata " + event.getMetadata + " from " + event.getRepository)
  }

  override def metadataDownloading(event: RepositoryEvent): Unit = {
    logger.info("Downloading metadata " + event.getMetadata + " from " + event.getRepository)
  }

  override def metadataInstalled(event: RepositoryEvent): Unit = {
    logger.info("Installed " + event.getMetadata + " to " + event.getFile)
  }

  override def metadataInstalling(event: RepositoryEvent): Unit = {
    logger.info("Installing " + event.getMetadata + " to " + event.getFile)
  }

  override def metadataInvalid(event: RepositoryEvent): Unit = {
    logger.info("Invalid metadata " + event.getMetadata)
  }

  override def metadataResolved(event: RepositoryEvent): Unit = {
    logger.info("Resolved metadata " + event.getMetadata + " from " + event.getRepository)
  }

  override def metadataResolving(event: RepositoryEvent): Unit = {
    logger.info("Resolving metadata " + event.getMetadata + " from " + event.getRepository)
  }
}
