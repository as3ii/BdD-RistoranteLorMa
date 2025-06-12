{
  description = "";

  inputs = {
    nixpkgs.url = "github:nixos/nixpkgs/nixos-unstable";
    flake-parts.url = "github:hercules-ci/flake-parts";
    git-hooks-nix = {
      url = "github:cachix/git-hooks.nix";
      inputs.nixpkgs.follows = "nixpkgs";
    };
  };

  outputs = { flake-parts, git-hooks-nix, ... }@inputs:
    flake-parts.lib.mkFlake { inherit inputs; } {
      imports = [
        git-hooks-nix.flakeModule
      ];

      systems = [ "x86_64-linux" "aarch64-linux" ];

      perSystem = { config, pkgs, ... }:
        let
          jdk = pkgs.jdk21;
          gradle = pkgs.gradle.override { java = jdk; };
        in
        {
          formatter = pkgs.nixpkgs-fmt;

          pre-commit = {
            check.enable = true;

            settings = {
              addGcRoot = true;

              hooks = {
                # Misc
                check-added-large-files.enable = true;
                check-yaml.enable = true;
                detect-private-keys.enable = true;
                end-of-file-fixer.enable = true;
                ripsecrets.enable = true;
                trim-trailing-whitespace.enable = true;
                # Nix
                deadnix.enable = true;
                nil.enable = true;
                nixpkgs-fmt.enable = true;
                # Java/gradle
                # gradle-check = {
                #   enable = true;
                #   name = "gradle check";
                #   package = gradle;
                #   entry = "${gradle}/bin/gradle";
                #   args = [ "check" ];
                #   pass_filenames = false;
                #   types = [ "text" "java" ];
                # };
              };
            };
          };

          devShells.default = pkgs.mkShell {
            buildInputs = [
              jdk
              gradle
            ];
            shellHook = ''
              ${config.pre-commit.installationScript}
              export _JAVA_OPTIONS='-Dawt.useSystemAAFontSettings=lcd'
              echo 1>&2 "Welcome to the development shell!"
            '';
          };
        };

      flake = { };
    };
}
