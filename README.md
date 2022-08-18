# Welcome to XDAGJ

[![FOSSA Status](https://app.fossa.com/api/projects/git%2Bgithub.com%2FXDagger%2Fxdagj.svg?type=shield)](https://app.fossa.com/projects/git%2Bgithub.com%2FXDagger%2Fxdagj?ref=badge_shield) ![](https://github.com/XDagger/xdagj/actions/workflows/maven.yml/badge.svg) ![GitHub release (latest SemVer)](https://img.shields.io/github/v/release/XDagger/xdagj) ![GitHub](https://img.shields.io/github/license/XDagger/xdagj) ![GitHub issues](https://img.shields.io/github/issues/XDagger/xdagj)

[中文版](./docs/README_zh.md)


## Directory
  - [System environment](#system-environment)
  - [Installation and usage](#installation-and-usage)
  - [Develop](#develop)
    - [XDAG Mars Project](#XDAG-Mars-Project)
  - [Code](#code)
  - [Contribution](#contribution)
  - [Sponsorship](#sponsorship)
  - [Other](#other)
  - [License](#license)

## System environment
```yaml
JDK   : v17
Maven : v3.8.3
```
## Installation and usage

XDAGJ Testnet Tutorial can help you quickly access XDAGJ testnet by using wallets and mining functions.
The Private Chain Building Tutorial helps you to build a private-chain for testing XDAGJ and finding bugs.

[XDAGJ_TestNet_Tutorial](./docs/XDAGJ_TestNet_Tutorial_en.md)

[XDAGJ_Devnet_Tutorial](./docs/XDAGJ_Devnet_Tutorial_en.md)

[XDAGJ TestNet Explorer](http://146.56.240.230/)

## Develop

XDAGJ has the basic functions as a pool. Follow-up works will improve the stability of the system and add new functions. The aim is to prepar for XDAG to enter the metaverse. It is important to adopt excellent blockchain technology.

### XDAG Mars Project

Four stages：

#### Exploration phase: XDAGJ testnet is online (online, in beta)

- [x] Deploy XDAGJ test network environment, open beta

- [x] Implement the RandomX algorithm

- [x] Implement the libp2p network protocol

- [x] Testnet blockchain browser

- [x] Test coin acquisition


#### Landing phase: XDAGJ mainnet is online

- [x] Add test cases: write test cases for existing functions

- [x] Add log functions: provide a relatively complete log service to facilitate trouble shooting

- [x] Optimize synchronization protocol: ameliorate the existing synchronization protocol and improve synchronization efficiency

- [x] Implement the snapshot function: reduce the cost of runing a mining pool and boost the loading process

- [x] Implement the RPC function: access to Web3j, realize the standardization of the interface

- [x] Introduce the Stratum protocol for miners

- [ ] Lightweight wallet application: connect to MateMask, join the browser wallet

- [x] Standardize the format of public and private keys, follow the BIPXX specification, and add mnemonic words to generate public and private key pairs

#### Expansion phase: XDAGJ & EVM 

- [ ] Improve the address block structure and increase the handling fee

- [x] Optimize and improve mobile wallets to improve user experience

- [ ] Support smart contracts, implement EVM that supports Solidity, and be compatible with Ethereum smart contracts

- [ ] Lower the threshold of mining pool users and gradually open the whitelist to achieve complete decentralization

#### Prosperity phase: XDAGJ & DeFi

- [ ] Implement cross-chain protocols, compatible with access to multiple blockchain systems, to achieve intercommunication between XDAG and other chain worlds

- [ ] Implement the oracle function

- [ ] Join a distributed exchange


## Code

- Git

  We use the gitflow branch model

  - `master` is the main branch, which is also used to deploy the production environment. Cannot modify the code directly at any time.
  - `develop` is the development branch, always keep the latest code after completion and bug fixes.
  - `feature` is a new feature branch. When developing new features, use the `develop` branch as the basis, and create the corresponding `feature/xxx` branch according to the development characteristics.
  - `release` is the pre-launch branch. During the release test phase, the release branch code will be used as the benchmark test. When a set of features is developed, it will be merged into the develop branch first, and a release branch will be created when entering the test. If there is a bug that needs to be fixed during the testing process, it will be directly fixed and submitted by the developer in the release branch. When the test is completed, merge the release branch to the master and develop branches. At this time, the master is the latest code and is used to go online.
  - `hotfix` is the branch for repairing urgent problems on the line. Using the `master` branch as the baseline, create a `hotfix/xxx` branch. After the repair is completed, it needs to be merged into the `master` branch and the `develop` branch.

- Commit Message

  The submission message must begin with a short subject line, followed by an optional, more detailed explanatory text, which is separated from the abstract by a blank line.

- Pull Request

  The pull request must be as clear and detailed as possible, including all related issues. If the pull request is to close an issue, please use the Github keyword convention [close, fix, or resolve](https://help.github.com/articles/closing-issues-via-commit-messages/). If the pull request only completes part of the problem, use the `connected` keyword. This helps our tool to correctly link the issue to the pull request.

- Code Style

  Use the `formatter_eclipse.xml` or `formatter_intellij.xml` of the `xdagj` code style in the `misc/code-style` folder.

- Code Review

  We value the quality and accuracy of the code. Therefore, we will review all the code that needs to be changed.

## FAQ

- Time Synchronization Question

[XDAGJ_Time_Synchronization](./docs/XDAGJ_Time_Synchronization_en.md)

## Contribution

- Security Question

  XDAGJ is still in the process of large-scale development, which means that there may be problems with existing codes or protocols, or errors that may exist in practice. If you find a security problem, we hope you can give it back as soon as possible.

  If you find a problem that may affect the security of the deployed system, we hope that you can send the problem privately to xdagj@xdag.io. Please do not discuss it publicly!

  If the problem is a weakness of the agreement or does not affect the online system, it can be discussed publicly and posted to [issues](https://github.com/XDagger/xdagj.git).

- features

  We are very happy to add more useful and interesting new features to XDAGJ. You can talk about any interesting new features.

If you are interested in the development of XDAGJ, we also welcome you to join the developer team and contribute your strength to XDAGJ. You can get in touch with us at xdagj@xdag.io .

## Sponsorship

Since the Apollo Project was launched, XDAGJ has achieved a 0 to 1 breakthrough. The development of XDAGJ has been on the right track. Your support can drive a successful technology implementation of XDAGJ.

XDAG：+89Zijf2XsXqbdVK7rdfR4F8+RkHkAPh

## Other

[XDAGJ Libp2p Introduction](./docs/XDAGJ_Networking_Specification.md)

[XDAG WIKI](https://github.com/XDagger/xdag/wiki)

[XDAG Whitepaper](https://github.com/XDagger/xdag/blob/master/WhitePaper.md)

[XDAG Protocol](https://github.com/XDagger/xdag/blob/master/Protocol.md)



## License

[![FOSSA Status](https://app.fossa.com/api/projects/git%2Bgithub.com%2FXDagger%2Fxdagj.svg?type=large)](https://app.fossa.com/projects/git%2Bgithub.com%2FXDagger%2Fxdagj?ref=badge_large)

