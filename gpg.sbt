inThisBuild(
  List(
    PgpKeys.pgpSelectPassphrase :=
      sys.props
        .get("SIGNING_KEY_PASSPHRASE")
        .map(_.toCharArray),
    usePgpKeyHex(System.getenv("SIGNING_KEY_ID"))
  )
)
