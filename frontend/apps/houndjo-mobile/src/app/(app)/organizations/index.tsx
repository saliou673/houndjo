import { useEffect, useState } from 'react';
import { ScrollView, StyleSheet } from 'react-native';
import { useTranslation } from 'react-i18next';
import { axiosInstance } from '@api-client';
import { Card } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Text } from '@/components/ui/text';

type Organization = { id: number; name: string; contactEmail: string; phoneNumber?: string; address?: string; defaultCurrencyCode: string; defaultLanguageKey: string };
export default function OrganizationsScreen() {
  const { t } = useTranslation();
  const [organizations, setOrganizations] = useState<Organization[]>([]);
  const [selected, setSelected] = useState<Organization | null>(null);
  const [name, setName] = useState('');
  const [contactEmail, setContactEmail] = useState('');
  const [loading, setLoading] = useState(false);
  const load = async () => { const { data } = await axiosInstance.get<Organization[]>('/api/organizations/mine'); setOrganizations(data); if (!selected && data[0]) setSelected(data[0]); };
  useEffect(() => { void load(); }, []);
  async function save() { setLoading(true); try { const { data } = selected ? await axiosInstance.put<Organization>(`/api/organizations/${selected.id}`, { name: selected.name, contactEmail: selected.contactEmail }) : await axiosInstance.post<Organization>('/api/organizations/register', { name, contactEmail }); setSelected(data); await load(); } finally { setLoading(false); } }
  return <ScrollView contentContainerStyle={styles.content}><Text variant="title">{t('organizations.title')}</Text><Text themeColor="textSecondary">{t('organizations.description')}</Text><Card><Text variant="subtitle">{t('organizations.schools')}</Text>{organizations.map((organization) => <Button key={organization.id} variant={selected?.id === organization.id ? 'default' : 'outline'} label={organization.name} onPress={() => setSelected(organization)} />)}{organizations.length === 0 && <Text themeColor="textSecondary">{t('organizations.empty')}</Text>}</Card><Card><Text variant="subtitle">{selected ? t('organizations.edit') : t('organizations.create')}</Text><Input label={t('organizations.name')} value={selected?.name ?? name} onChangeText={(value) => selected ? setSelected({ ...selected, name: value }) : setName(value)} /><Input label={t('organizations.email')} keyboardType="email-address" value={selected?.contactEmail ?? contactEmail} onChangeText={(value) => selected ? setSelected({ ...selected, contactEmail: value }) : setContactEmail(value)} /><Button label={loading ? t('organizations.saving') : t('organizations.save')} loading={loading} onPress={() => void save()} /></Card></ScrollView>;
}
const styles = StyleSheet.create({ content: { padding: 16, gap: 16 }, });
