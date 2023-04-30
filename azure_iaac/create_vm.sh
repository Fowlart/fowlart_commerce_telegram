az group create --name my_resources_north_europe_1 --location northeurope

az network nic create --resource-group my_resources_north_europe_1 --name art-home-nic_1 --vnet-name art-home-vnet --subnet art-home-subnet --public-ip-address art-home-pip_1 --network-security-group art-home-nsg_1

az network vnet create --resource-group my_resources_north_europe_1 --name art-home-vnet_1 --address-prefix 10.0.0.0/16 --subnet-name art-home-subnet --subnet-prefix 10.0.1.0/24

az network nsg create --resource-group my_resources_north_europe_1 --name art-home-nsg_1

az network nsg rule create --resource-group my_resources_north_europe_1 --nsg-name art-home-nsg_1 --name allow-ssh --priority 100 --destination-port-ranges 22 --access Allow --protocol Tcp --description "Allow SSH traffic"

az network public-ip create --resource-group my_resources_north_europe_1 --name art-home-pip_1 --sku Standard --allocation-method Static --dns-name art-home-host

az vm create --resource-group my_resources_north_europe_1 --name art-home-host_1 --location northeurope --size Standard_B1s --image canonical:0001-com-ubuntu-server-focal:20_04-lts-gen2:latest --os-disk-name art-home-host_OsDisk_1_6d26cc1f6c7e4d9a889e753f0f9eaac4 --storage-sku StandardSSD_LRS --nics art-home-nic_1 --admin-username fowlart1988 --generate-ssh-keys

az vm extension set --publisher Microsoft.Azure.NetworkWatcher --name NetworkWatcherAgentLinux --version 1.4 --vm-name art-home-host_1 --resource-group my_resources_north_europe_1

az vm extension set --publisher Microsoft.OSTCExtensions --name VMAccessForLinux --version 1.4 --vm-name art-home-host_1 --resource-group my_resources_north_europe_1
